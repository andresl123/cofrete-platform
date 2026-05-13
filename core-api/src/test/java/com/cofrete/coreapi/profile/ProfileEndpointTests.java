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
