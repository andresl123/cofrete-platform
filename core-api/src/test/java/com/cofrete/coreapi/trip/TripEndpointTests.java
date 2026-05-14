package com.cofrete.coreapi.trip;

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class TripEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRecalculationEventRepository recalculationEvents;

    @Test
    void tripApisRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "trip-create@example.test")
    void createsTripAndPublishesCanonicalRecalculationEvent() throws Exception {
        createDriver("Trip Create").andExpect(status().isCreated());
        String truckId = createTruck().andReturn().getResponse().getContentAsString();
        String truck = objectMapper.readTree(truckId).path("truck").path("id").asText();

        JsonNode trip = andReturnJson(createTrip(truck)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trip.origin.city").value("Goiania"))
            .andExpect(jsonPath("$.trip.destination.state").value("SP"))
            .andExpect(jsonPath("$.trip.grossFreight").value("8000.00"))
            .andExpect(jsonPath("$.trip.inputRevision").value(1))
            .andExpect(jsonPath("$.trip.decisionState").value("DRAFT")))
            .path("trip");

        mockMvc.perform(get("/api/trips/{tripId}", trip.path("id").asText()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trip.truckId").value(truck));

        var events = recalculationEvents.findByTripIdOrderByInputRevisionAsc(trip.path("id").asText());
        assertThat(events).hasSize(1);
        var event = events.getFirst();
        assertThat(event.getEventType()).isEqualTo("trip.recalculation.requested");
        assertThat(event.getVersion()).isEqualTo(1);
        assertThat(event.getEventId()).startsWith("evt_");
        assertThat(event.getIdempotencyKey()).isEqualTo("trip:" + trip.path("id").asText() + ":recalculation:1");
        assertThat(event.getTruckId()).isEqualTo(truck);
        assertThat(event.getInputRevision()).isEqualTo(1);
        assertThat(event.getReason()).isEqualTo(RecalculationReason.TRIP_CREATED);
        assertThat(event.getCorrelationId()).isEqualTo("corr-trip-create");
        assertThat(event.getProducer()).isEqualTo("core-api");
        assertThat(event.getRequestedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "trip-profit@example.test")
    void estimatesAndPersistsProfitabilitySnapshotWithoutCountingPassThroughAsProfit() throws Exception {
        createDriver("Trip Profit").andExpect(status().isCreated());
        String truck = objectMapper.readTree(createTruck().andReturn().getResponse().getContentAsString())
            .path("truck")
            .path("id")
            .asText();
        String tripId = andReturnJson(createTrip(truck)).path("trip").path("id").asText();

        mockMvc.perform(get("/api/trips/{tripId}/profitability-snapshot", tripId))
            .andExpect(status().isTooEarly())
            .andExpect(jsonPath("$.error").value("SNAPSHOT_PENDING"));

        estimate(tripId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profitabilityEstimate.tripId").value(tripId))
            .andExpect(jsonPath("$.profitabilityEstimate.grossFreight").value("8000.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.passThroughAmount").value("600.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.directTripCost").value("3000.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.requiredReserves").value("1776.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.safePersonalWithdrawal").value("1974.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.expectedProfit").value("1974.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.marginPercent").value("24.68"))
            .andExpect(jsonPath("$.profitabilityEstimate.financialHealthStatus").value("GOOD"))
            .andExpect(jsonPath("$.profitabilityEstimate.recommendation").value("ACCEPT"))
            .andExpect(jsonPath("$.profitabilityEstimate.caveats", hasSize(2)));

        mockMvc.perform(get("/api/trips/{tripId}/profitability-snapshot", tripId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profitabilitySnapshot.tripId").value(tripId))
            .andExpect(jsonPath("$.profitabilitySnapshot.inputRevision").value(2))
            .andExpect(jsonPath("$.profitabilitySnapshot.calculationTraceId").value(org.hamcrest.Matchers.startsWith("calc_")))
            .andExpect(jsonPath("$.profitabilitySnapshot.sourceMetadata[?(@.area=='fuel')].freshnessStatus").value("CURRENT"));

        var events = recalculationEvents.findByTripIdOrderByInputRevisionAsc(tripId);
        assertThat(events).hasSize(2);
        assertThat(events.get(1).getIdempotencyKey()).isEqualTo("trip:" + tripId + ":recalculation:2");
        assertThat(events.get(1).getReason()).isEqualTo(RecalculationReason.MANUAL_RECALCULATION);
    }

    @Test
    @WithMockUser(username = "trip-decision@example.test")
    void recordsAcceptanceDecisionAndUpdatesTripState() throws Exception {
        createDriver("Trip Decision").andExpect(status().isCreated());
        String truck = objectMapper.readTree(createTruck().andReturn().getResponse().getContentAsString())
            .path("truck")
            .path("id")
            .asText();
        String tripId = andReturnJson(createTrip(truck)).path("trip").path("id").asText();

        mockMvc.perform(post("/api/trips/{tripId}/acceptance-decision", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision": "RENEGOTIATE",
                      "reasonCodes": ["LOW_MARGIN", "PAYMENT_TERM"],
                      "note": "Ask for a higher freight amount."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acceptanceDecision.decision").value("RENEGOTIATE"))
            .andExpect(jsonPath("$.acceptanceDecision.reasonCodes", hasSize(2)));

        mockMvc.perform(get("/api/trips/{tripId}", tripId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trip.decisionState").value("RENEGOTIATION"));

        mockMvc.perform(post("/api/trips/{tripId}/acceptance-decision", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision": "ACCEPT",
                      "reasonCodes": ["UPDATED_CHOICE"]
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @WithMockUser(username = "trip-tolls@example.test")
    void recordsTollAndValePedagioClassificationsWithoutInflatingProfit() throws Exception {
        createDriver("Trip Tolls").andExpect(status().isCreated());
        String truck = objectMapper.readTree(createTruck().andReturn().getResponse().getContentAsString())
            .path("truck")
            .path("id")
            .asText();
        String tripId = andReturnJson(createTrip(truck)).path("trip").path("id").asText();

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .header("X-Correlation-Id", "corr-toll-pass-through")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plazaName": "Praca de Pedagio Example",
                      "amount": "385.70",
                      "currency": "BRL",
                      "paidBy": "DRIVER",
                      "classification": "PASS_THROUGH",
                      "confidence": "driver_receipt",
                      "sourceReference": "receipt_123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tripToll.classification").value("PASS_THROUGH"))
            .andExpect(jsonPath("$.tripToll.financeTreatment").value("pass_through_or_reimbursement_not_profit"))
            .andExpect(jsonPath("$.tripToll.confidence").value("driver_receipt"));

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .header("X-Correlation-Id", "corr-toll-driver-paid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plazaName": "Manual toll paid by driver",
                      "amount": "300.00",
                      "currency": "BRL",
                      "paidBy": "DRIVER",
                      "classification": "DRIVER_PAID_NON_REIMBURSED"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tripToll.financeTreatment").value("driver_cost_reduces_profit"));

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .header("X-Correlation-Id", "corr-toll-included")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plazaName": "Included toll component",
                      "amount": "100.00",
                      "currency": "BRL",
                      "paidBy": "SHIPPER",
                      "classification": "INCLUDED_IN_FREIGHT",
                      "confidence": "payer_statement"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tripToll.financeTreatment").value("warning_may_distort_profit"));

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .header("X-Correlation-Id", "corr-toll-unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plazaName": "Unverified toll",
                      "amount": "50.00",
                      "currency": "BRL",
                      "paidBy": "UNKNOWN",
                      "classification": "UNKNOWN"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tripToll.financeTreatment").value("manual_review_needed"));

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .header("X-Correlation-Id", "corr-no-toll")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plazaName": "No toll segment",
                      "amount": "0.00",
                      "currency": "BRL",
                      "paidBy": "UNKNOWN",
                      "classification": "NO_TOLL"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tripToll.financeTreatment").value("no_toll_cost"));

        mockMvc.perform(post("/api/trips/{tripId}/vale-pedagio", tripId)
                .header("X-Correlation-Id", "corr-vale-pedagio")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "provider": "Shipper Example",
                      "proofReference": "vale_456",
                      "amount": "214.30",
                      "currency": "BRL",
                      "receivedStatus": "VALE_PEDAGIO_RECEIVED",
                      "confidence": "proof_received"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.valePedagioRecord.classification").value("PASS_THROUGH"))
            .andExpect(jsonPath("$.valePedagioRecord.financeTreatment").value("vale_pedagio_pass_through_not_profit"));

        mockMvc.perform(get("/api/trips/{tripId}/tolls", tripId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tripTolls", hasSize(5)))
            .andExpect(jsonPath("$.valePedagioRecords", hasSize(1)))
            .andExpect(jsonPath("$.classificationTotals.passThroughAmount").value("385.70"))
            .andExpect(jsonPath("$.classificationTotals.driverPaidNonReimbursedAmount").value("300.00"))
            .andExpect(jsonPath("$.classificationTotals.includedInFreightAmount").value("100.00"))
            .andExpect(jsonPath("$.classificationTotals.noTollAmount").value("0.00"))
            .andExpect(jsonPath("$.classificationTotals.unknownAmount").value("50.00"))
            .andExpect(jsonPath("$.classificationTotals.valePedagioPassThroughAmount").value("214.30"))
            .andExpect(jsonPath("$.classificationTotals.financeTreatment")
                .value("toll_reimbursement_and_vale_pedagio_excluded_from_profit"));

        estimateWithoutTollOverrides(tripId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profitabilityEstimate.passThroughAmount").value("700.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.directTripCost").value("3050.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.requiredReserves").value("1752.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.safePersonalWithdrawal").value("1848.00"))
            .andExpect(jsonPath("$.profitabilityEstimate.expectedProfit").value("1848.00"));

        var events = recalculationEvents.findByTripIdOrderByInputRevisionAsc(tripId);
        assertThat(events).hasSize(8);
        assertThat(events.subList(1, 7))
            .extracting(TripRecalculationEventRecord::getReason)
            .containsOnly(RecalculationReason.TOLL_CLASSIFICATION_UPDATED);
        assertThat(events.get(7).getIdempotencyKey()).isEqualTo("trip:" + tripId + ":recalculation:8");
    }

    @Test
    @WithMockUser(username = "toll-estimate@example.test")
    void exposesAdvisoryTollEstimateAndImportStatusWithoutPaidProviderIntegration() throws Exception {
        mockMvc.perform(post("/api/toll-estimates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "origin": {"city": "Goiania", "state": "GO"},
                      "destination": {"city": "Sao Paulo", "state": "SP"},
                      "distanceKm": "920.00",
                      "axles": 6,
                      "vehicleType": "truck"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tollEstimate.totalEstimatedToll").value("0.00"))
            .andExpect(jsonPath("$.tollEstimate.freshnessStatus").value("UNKNOWN"))
            .andExpect(jsonPath("$.tollEstimate.confidence").value("no_imported_toll_data_available"))
            .andExpect(jsonPath("$.tollEstimate.financeTreatment").value("pass_through_or_reimbursement_not_profit"));

        mockMvc.perform(get("/api/toll-data/import-status?source=ANTT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tollDataImportStatus.source").value("ANTT"))
            .andExpect(jsonPath("$.tollDataImportStatus.dataset").value("toll_plazas_and_tariffs"))
            .andExpect(jsonPath("$.tollDataImportStatus.freshnessStatus").value("UNKNOWN"))
            .andExpect(jsonPath("$.tollDataImportStatus.confidence").value("not_imported"));
    }

    @Test
    @WithMockUser(username = "trip-validation@example.test")
    void validatesTripAndProfitabilityInputs() throws Exception {
        createDriver("Trip Validation").andExpect(status().isCreated());

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "truckId": "truck_missing",
                      "origin": {"city": "Goiania", "state": "GO"},
                      "destination": {"city": "Sao Paulo", "state": "SP"},
                      "loadedKm": "920.00",
                      "emptyKm": "80.00",
                      "grossFreight": "8000.00",
                      "currency": "BRL"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        String truck = objectMapper.readTree(createTruck().andReturn().getResponse().getContentAsString())
            .path("truck")
            .path("id")
            .asText();
        String tripId = andReturnJson(createTrip(truck)).path("trip").path("id").asText();

        mockMvc.perform(post("/api/trips/{tripId}/profitability-estimate", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "dieselConsumptionKmPerLiter": "2.5000",
                      "dieselPricePerLiter": "5.2500",
                      "tollReimbursement": "9000.00",
                      "reservePolicy": {
                        "maintenanceRate": "0.080000",
                        "tireRate": "0.040000",
                        "taxRate": "0.030000",
                        "insuranceRate": "0.020000",
                        "replacementRate": "0.050000",
                        "emergencyRate": "0.020000"
                      }
                    }
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("CALCULATION_UNAVAILABLE"));

        mockMvc.perform(post("/api/trips/{tripId}/tolls/manual-payment", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "amount": "10.00",
                      "currency": "BRL",
                      "paidBy": "DRIVER",
                      "classification": "NO_TOLL"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private ResultActions createDriver(String name) throws Exception {
        return mockMvc.perform(post("/api/drivers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "email": "driver@example.test",
                  "phone": "+55 62 99999-0000",
                  "state": "GO",
                  "documentType": "CPF",
                  "cpfCnpjLast4": "4321",
                  "rntrcNumber": "12345678",
                  "rntrcCategory": "TAC",
                  "rntrcStatus": "UNKNOWN",
                  "active": true
                }
                """.formatted(name)));
    }

    private ResultActions createTruck() throws Exception {
        return mockMvc.perform(post("/api/trucks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "plate": "ABC1D23",
                  "renavamLast4": "6789",
                  "state": "GO",
                  "axleCount": 6,
                  "vehicleType": "TRUCK",
                  "fuelType": "DIESEL_S10",
                  "active": true
                }
                """));
    }

    private ResultActions createTrip(String truckId) throws Exception {
        return mockMvc.perform(post("/api/trips")
            .header("X-Correlation-Id", "corr-trip-create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "truckId": "%s",
                  "origin": {"city": "Goiania", "state": "GO"},
                  "destination": {"city": "Sao Paulo", "state": "SP"},
                  "loadedKm": "920.00",
                  "emptyKm": "80.00",
                  "grossFreight": "8000.00",
                  "currency": "BRL",
                  "advanceAmount": "3000.00",
                  "balanceDueDays": 21,
                  "cargoDescription": "general cargo",
                  "expectedPickupAt": "2026-05-20T12:00:00Z"
                }
                """.formatted(truckId)));
    }

    private ResultActions estimate(String tripId) throws Exception {
        return mockMvc.perform(post("/api/trips/{tripId}/profitability-estimate", tripId)
            .header("X-Correlation-Id", "corr-trip-estimate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "dieselConsumptionKmPerLiter": "2.5000",
                  "dieselPricePerLiter": "5.2500",
                  "arlaCost": "120.00",
                  "nonReimbursedToll": "300.00",
                  "tollReimbursement": "385.70",
                  "valePedagio": "214.30",
                  "otherPassThrough": "0.00",
                  "mealsAndLodgingCost": "280.00",
                  "otherDirectCost": "200.00",
                  "financingAllocation": "650.00",
                  "reservePolicy": {
                    "maintenanceRate": "0.080000",
                    "tireRate": "0.040000",
                    "taxRate": "0.030000",
                    "insuranceRate": "0.020000",
                    "replacementRate": "0.050000",
                    "emergencyRate": "0.020000"
                  },
                  "sourceFreshness": {
                    "fuel": "CURRENT",
                    "toll": "CURRENT",
                    "tax": "CURRENT",
                    "compliance": "UNKNOWN"
                  }
                }
                """));
    }

    private ResultActions estimateWithoutTollOverrides(String tripId) throws Exception {
        return mockMvc.perform(post("/api/trips/{tripId}/profitability-estimate", tripId)
            .header("X-Correlation-Id", "corr-trip-estimate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "dieselConsumptionKmPerLiter": "2.5000",
                  "dieselPricePerLiter": "5.2500",
                  "arlaCost": "120.00",
                  "otherPassThrough": "0.00",
                  "mealsAndLodgingCost": "280.00",
                  "otherDirectCost": "200.00",
                  "financingAllocation": "650.00",
                  "reservePolicy": {
                    "maintenanceRate": "0.080000",
                    "tireRate": "0.040000",
                    "taxRate": "0.030000",
                    "insuranceRate": "0.020000",
                    "replacementRate": "0.050000",
                    "emergencyRate": "0.020000"
                  },
                  "sourceFreshness": {
                    "fuel": "CURRENT",
                    "toll": "CURRENT",
                    "tax": "CURRENT",
                    "compliance": "UNKNOWN"
                  }
                }
                """));
    }

    private JsonNode andReturnJson(ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
    }
}
