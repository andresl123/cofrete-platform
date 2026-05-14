package com.cofrete.coreapi.imports;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
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
class FuelDataEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fuelDataApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/fuel-prices/latest")
                .queryParam("fuel", "DIESEL_S10")
                .queryParam("state", "GO"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "fuel-data@example.test")
    void servesLatestAndHistoryFuelPricesWithSourceMetadata() throws Exception {
        mockMvc.perform(get("/api/fuel-prices/latest")
                .queryParam("fuel", "DIESEL_S10")
                .queryParam("state", "GO")
                .queryParam("city", "GOIANIA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fuelPrice.pricePerLiter").value("6.1800"))
            .andExpect(jsonPath("$.fuelPrice.source").value("ANP"))
            .andExpect(jsonPath("$.fuelPrice.sourceType").value("official_dataset"))
            .andExpect(jsonPath("$.fuelPrice.sourcePeriodStart").value("2026-05-03"))
            .andExpect(jsonPath("$.fuelPrice.sourcePeriodEnd").value("2026-05-09"))
            .andExpect(jsonPath("$.fuelPrice.freshnessStatus").value("CURRENT"))
            .andExpect(jsonPath("$.fuelPrice.confidence").value("SYNTHETIC_FIXTURE"))
            .andExpect(jsonPath("$.fuelPrice.importAuditId").value("import_anp_synthetic_20260509"));

        mockMvc.perform(get("/api/fuel-prices/history")
                .queryParam("fuel", "DIESEL_S10")
                .queryParam("state", "GO")
                .queryParam("city", "GOIANIA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prices", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "fuel-report@example.test")
    void recordsDriverFuelReportWithoutPresentingItAsAnpData() throws Exception {
        createDriver("Fuel Report").andExpect(status().isCreated());

        mockMvc.perform(post("/api/fuel-prices/driver-report")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fuelType": "DIESEL_S10",
                      "state": "GO",
                      "city": "GOIANIA",
                      "stationName": "Driver Station",
                      "pricePerLiter": "6.0500",
                      "currency": "BRL",
                      "receiptReference": "receipt_789",
                      "reportedAt": "2026-05-12T10:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.driverFuelReport.source").value("driver_report"))
            .andExpect(jsonPath("$.driverFuelReport.sourceType").value("driver_report"))
            .andExpect(jsonPath("$.driverFuelReport.sourceUrl").doesNotExist())
            .andExpect(jsonPath("$.driverFuelReport.sourcePeriodStart").doesNotExist())
            .andExpect(jsonPath("$.driverFuelReport.sourcePeriodEnd").doesNotExist())
            .andExpect(jsonPath("$.driverFuelReport.freshnessStatus").value("CURRENT"))
            .andExpect(jsonPath("$.driverFuelReport.confidence").value("driver_confirmed"))
            .andExpect(jsonPath("$.driverFuelReport.pricePerLiter").value("6.0500"));
    }

    @Test
    @WithMockUser(username = "fuel-estimate@example.test")
    void estimatesTripFuelFromRouteTruckConsumptionAndAnpPrice() throws Exception {
        createDriver("Fuel Estimate").andExpect(status().isCreated());
        String truck = objectMapper.readTree(createTruck().andReturn().getResponse().getContentAsString())
            .path("truck")
            .path("id")
            .asText();
        String tripId = andReturnJson(createTrip(truck)).path("trip").path("id").asText();

        mockMvc.perform(post("/api/trips/{tripId}/fuel-estimate", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "routeKm": "850.00",
                      "loadStatus": "LOADED",
                      "safetyMarginPercent": "10.00"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fuelEstimate.tripId").value(tripId))
            .andExpect(jsonPath("$.fuelEstimate.truckId").value(truck))
            .andExpect(jsonPath("$.fuelEstimate.routeKm").value("850.00"))
            .andExpect(jsonPath("$.fuelEstimate.consumptionKmPerLiter").value("2.3500"))
            .andExpect(jsonPath("$.fuelEstimate.dieselPricePerLiter").value("6.1800"))
            .andExpect(jsonPath("$.fuelEstimate.estimatedLiters").value("361.70"))
            .andExpect(jsonPath("$.fuelEstimate.estimatedFuelCost").value("2235.31"))
            .andExpect(jsonPath("$.fuelEstimate.recommendedFuelBudget").value("2458.84"))
            .andExpect(jsonPath("$.fuelEstimate.priceSource").value("ANP"))
            .andExpect(jsonPath("$.fuelEstimate.freshnessStatus").value("CURRENT"))
            .andExpect(jsonPath("$.fuelEstimate.importAuditId").value("import_anp_synthetic_20260509"))
            .andExpect(jsonPath("$.fuelEstimate.calculationTraceId").value(startsWith("calc_fuel_")));
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
                  "plate": "DEF1G23",
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
                  "currency": "BRL"
                }
                """.formatted(truckId)));
    }

    private JsonNode andReturnJson(ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
    }
}
