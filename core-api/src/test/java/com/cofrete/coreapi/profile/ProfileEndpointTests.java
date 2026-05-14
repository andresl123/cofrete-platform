package com.cofrete.coreapi.profile;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class ProfileEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void profileApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/drivers/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "driver-create@example.test")
    void createsAndFetchesCurrentDriverWithMaskedComplianceIdentifiers() throws Exception {
        createDriver("Driver Create", "GO", "12345678")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.driver.name").value("Driver Create"))
            .andExpect(jsonPath("$.driver.state").value("GO"))
            .andExpect(jsonPath("$.driver.cpfCnpjMasked").value("***4321"))
            .andExpect(jsonPath("$.driver.rntrcNumberMasked").value("***5678"))
            .andExpect(jsonPath("$.driver.rntrcSource").value("driver_entered"))
            .andExpect(jsonPath("$.driver.advisoryText").value(
                "RNTRC metadata is app-maintained. Confirm official status in ANTT/RNTRC Digital channels."));

        mockMvc.perform(get("/api/drivers/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.driver.name").value("Driver Create"));
    }

    @Test
    @WithMockUser(username = "driver-patch@example.test")
    void patchesCurrentDriver() throws Exception {
        createDriver("Driver Patch", "GO", "22345678").andExpect(status().isCreated());

        mockMvc.perform(patch("/api/drivers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "phone": "+55 62 90000-0000",
                      "rntrcStatus": "ACTIVE"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.driver.phone").value("+55 62 90000-0000"))
            .andExpect(jsonPath("$.driver.rntrcStatus").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "driver-duplicate@example.test")
    void rejectsSecondDriverForSamePrincipal() throws Exception {
        createDriver("Driver One", "GO", "32345678").andExpect(status().isCreated());

        createDriver("Driver Two", "GO", "42345678")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @WithMockUser(username = "truck-owner@example.test")
    void createsListsReadsAndReplacesDriverTruck() throws Exception {
        createDriver("Truck Owner", "GO", "52345678").andExpect(status().isCreated());
        var truckId = createTruck("ABC1D23", true)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.truck.plateMasked").value("***D23"))
            .andExpect(jsonPath("$.truck.renavamMasked").value("***6789"))
            .andExpect(jsonPath("$.truck.axleCount").value(6))
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode truck = objectMapper.readTree(truckId).path("truck");

        mockMvc.perform(get("/api/trucks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trucks", hasSize(1)));

        mockMvc.perform(get("/api/trucks").queryParam("active", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trucks", hasSize(0)));

        mockMvc.perform(get("/api/trucks/{truckId}", truck.path("id").asText()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.truck.fuelType").value("DIESEL_S10"));

        mockMvc.perform(get("/api/trucks/{truckId}/consumption-profile", truck.path("id").asText()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.truckConsumptionProfile.truckId").value(truck.path("id").asText()))
            .andExpect(jsonPath("$.truckConsumptionProfile.loadedAvgKmPerLiter").value("2.3500"))
            .andExpect(jsonPath("$.truckConsumptionProfile.emptyAvgKmPerLiter").value("3.1000"))
            .andExpect(jsonPath("$.truckConsumptionProfile.confidence").value("default_by_fuel_type"));

        mockMvc.perform(put("/api/trucks/{truckId}", truck.path("id").asText())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plate": "ABC1D23",
                      "renavamLast4": "6789",
                      "state": "GO",
                      "axleCount": 6,
                      "vehicleType": "TRUCK",
                      "fuelType": "DIESEL_S500",
                      "active": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.truck.fuelType").value("DIESEL_S500"))
            .andExpect(jsonPath("$.truck.active").value(false));
    }

    @Test
    @WithMockUser(username = "tax-profile@example.test")
    void createsReplacesAndFetchesTaxProfile() throws Exception {
        createDriver("Tax Profile", "GO", "62345678").andExpect(status().isCreated());

        mockMvc.perform(post("/api/tax-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "regime": "MEI_CAMINHONEIRO",
                      "planningYear": 2026,
                      "annualGrossLimit": "251600.00",
                      "currency": "BRL",
                      "source": "Receita Federal",
                      "sourceType": "official_guidance",
                      "freshnessStatus": "CURRENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taxProfile.regime").value("MEI_CAMINHONEIRO"))
            .andExpect(jsonPath("$.taxProfile.annualGrossLimit").value("251600.00"))
            .andExpect(jsonPath("$.taxProfile.advisoryText").value(
                "Tax values are planning metadata. Confirm obligations with Receita Federal or a qualified accountant."));

        mockMvc.perform(post("/api/tax-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "regime": "PESSOA_FISICA_AUTONOMA",
                      "planningYear": 2026,
                      "currency": "BRL",
                      "freshnessStatus": "UNKNOWN"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taxProfile.regime").value("PESSOA_FISICA_AUTONOMA"));

        mockMvc.perform(get("/api/tax-profile").queryParam("year", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taxProfile.regime").value("PESSOA_FISICA_AUTONOMA"));
    }

    @Test
    @WithMockUser(username = "ipva-source-rule@example.test")
    void upsertsIpvaRuleAndReportsMissingOrStaleStateRules() throws Exception {
        mockMvc.perform(get("/api/source-rules/ipva")
                .queryParam("state", "GO")
                .queryParam("vehicleType", "TRUCK")
                .queryParam("effectiveYear", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ipvaRule.ipvaStatus").value("MISSING"))
            .andExpect(jsonPath("$.ipvaRule.advisoryText").value(
                "No source-backed state rule is configured. Do not estimate IPVA or licensing from a national hardcoded percentage."));

        mockMvc.perform(post("/api/source-rules/ipva")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "state": "GO",
                      "vehicleType": "TRUCK",
                      "effectiveYear": 2026,
                      "ratePercent": "1.2500",
                      "currency": "BRL",
                      "sourceUrl": "https://www.go.gov.br/ipva",
                      "reviewedAt": "2024-01-10T00:00:00Z",
                      "freshnessStatus": "CURRENT",
                      "licensingSourceUrl": "https://www.detran.go.gov.br",
                      "licensingReviewedAt": "2099-04-01T00:00:00Z",
                      "licensingFreshnessStatus": "CURRENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ipvaRule.ratePercent").value("1.2500"))
            .andExpect(jsonPath("$.ipvaRule.ipvaStatus").value("STALE"))
            .andExpect(jsonPath("$.ipvaRule.licensingStatus").value("CURRENT"))
            .andExpect(jsonPath("$.ipvaRule.advisoryText").value(
                "IPVA and licensing reminders are advisory. Confirm official values, due dates, and payment channels with the state DETRAN or SEFAZ."));
    }

    @Test
    @WithMockUser(username = "tax-source-rule@example.test")
    void upsertsMeiAndPessoaFisicaSourceBackedTaxRules() throws Exception {
        mockMvc.perform(post("/api/source-rules/tax-years")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "regime": "MEI_CAMINHONEIRO",
                      "planningYear": 2026,
                      "annualGrossLimit": "251600.00",
                      "currency": "BRL",
                      "formulaMetadata": "DAS and annual limit are data records, not code constants.",
                      "effectiveFrom": "2026-01-01",
                      "sourceUrl": "https://www.gov.br/empresas-e-negocios/pt-br/empreendedor/mei-caminhoneiro/mei-caminhoneiro-3",
                      "reviewedAt": "2099-05-01T00:00:00Z",
                      "freshnessStatus": "CURRENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taxRuleYear.regime").value("MEI_CAMINHONEIRO"))
            .andExpect(jsonPath("$.taxRuleYear.ruleStatus").value("CURRENT"));

        mockMvc.perform(post("/api/source-rules/tax-years")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "regime": "PESSOA_FISICA_AUTONOMA",
                      "planningYear": 2026,
                      "currency": "BRL",
                      "formulaMetadata": "Cargo transport taxable portion is configurable planning metadata.",
                      "cargoTransportTaxablePercent": "0.100000",
                      "effectiveFrom": "2026-01-01",
                      "sourceUrl": "https://www.gov.br/receitafederal",
                      "reviewedAt": "2099-05-01T00:00:00Z",
                      "freshnessStatus": "CURRENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taxRuleYear.regime").value("PESSOA_FISICA_AUTONOMA"))
            .andExpect(jsonPath("$.taxRuleYear.cargoTransportTaxablePercent").value("0.100000"))
            .andExpect(jsonPath("$.taxRuleYear.advisoryText").value(
                "Tax outputs are planning estimates. Confirm Receita Federal, state, municipal, and accountant guidance before filing or changing regime."));

        mockMvc.perform(get("/api/source-rules/tax-years")
                .queryParam("regime", "PESSOA_FISICA_AUTONOMA")
                .queryParam("planningYear", "2027"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taxRuleYear.ruleStatus").value("MISSING"));
    }

    @Test
    @WithMockUser(username = "driver-validation@example.test")
    void validatesRequiredProfileFields() throws Exception {
        mockMvc.perform(post("/api/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "state": "go",
                      "cpfCnpjLast4": "12"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "driver-unknown-field@example.test")
    void rejectsUnknownJsonFieldsOnWriteEndpoints() throws Exception {
        mockMvc.perform(post("/api/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Unknown Field",
                      "state": "GO",
                      "unexpected": "value"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private org.springframework.test.web.servlet.ResultActions createDriver(
        String name,
        String state,
        String rntrcNumber
    ) throws Exception {
        return mockMvc.perform(post("/api/drivers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "email": "driver@example.test",
                  "phone": "+55 62 99999-0000",
                  "state": "%s",
                  "documentType": "CPF",
                  "cpfCnpjLast4": "4321",
                  "rntrcNumber": "%s",
                  "rntrcCategory": "TAC",
                  "rntrcStatus": "UNKNOWN",
                  "active": true
                }
                """.formatted(name, state, rntrcNumber)));
    }

    private org.springframework.test.web.servlet.ResultActions createTruck(String plate, boolean active) throws Exception {
        return mockMvc.perform(post("/api/trucks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "plate": "%s",
                  "renavamLast4": "6789",
                  "state": "GO",
                  "axleCount": 6,
                  "vehicleType": "TRUCK",
                  "fuelType": "DIESEL_S10",
                  "active": %s
                }
                """.formatted(plate, active)));
    }
}
