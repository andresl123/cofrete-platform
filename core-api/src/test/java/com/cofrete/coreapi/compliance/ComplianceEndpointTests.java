package com.cofrete.coreapi.compliance;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
class ComplianceEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void complianceApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/compliance/profile"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "compliance-rntrc@example.test")
    void savesRntrcMetadataWithOfficialChannelWording() throws Exception {
        createDriver("Compliance RNTRC", "12345678").andExpect(status().isCreated());

        mockMvc.perform(put("/api/compliance/rntrc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "rntrcNumber": "987654321",
                      "rntrcCategory": "TAC",
                      "rntrcStatus": "ACTIVE",
                      "ciotRequired": "UNKNOWN",
                      "latestCiotStatus": "NOT_RECORDED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rntrcProfile.numberMasked").value("***4321"))
            .andExpect(jsonPath("$.rntrcProfile.category").value("TAC"))
            .andExpect(jsonPath("$.rntrcProfile.status").value("ACTIVE"))
            .andExpect(jsonPath("$.rntrcProfile.guidance", hasItem(
                "Your Cofrete profile is not an official ANTT record. Official updates must be done through ANTT/RNTRC Digital.")))
            .andExpect(jsonPath("$.rntrcProfile.guidance", hasItem(
                "To update your RNTRC, access RNTRC Digital using your gov.br account, level prata or ouro.")));

        mockMvc.perform(get("/api/compliance/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complianceProfile.rntrc.numberMasked").value("***4321"))
            .andExpect(jsonPath("$.complianceProfile.score.score.value").value(75))
            .andExpect(jsonPath("$.complianceProfile.score.score.status").value("ATTENTION"));
    }

    @Test
    @WithMockUser(username = "compliance-check@example.test")
    void publicRntrcStatusCheckReturnsUnsupportedOfficialChannelGuidance() throws Exception {
        createDriver("Compliance Check", "22345678").andExpect(status().isCreated());

        mockMvc.perform(post("/api/compliance/rntrc/check-public-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "rntrcNumber": "22345678",
                      "rntrcCategory": "TAC",
                      "consentToPublicLookup": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicStatusCheck.status").value("UNSUPPORTED"))
            .andExpect(jsonPath("$.publicStatusCheck.officialActionUrl").value("https://consultapublica.antt.gov.br/"))
            .andExpect(jsonPath("$.publicStatusCheck.advisoryText").value(
                "Cofrete does not store gov.br credentials or update ANTT records. Use official ANTT consultation or RNTRC Digital for official status."));
    }

    @Test
    @WithMockUser(username = "compliance-insurance@example.test")
    void savesInsurancePolicyAndGeneratesExpirationCalendarAlert() throws Exception {
        createDriver("Compliance Insurance", "32345678").andExpect(status().isCreated());
        var expiresOn = LocalDate.now().plusDays(30);

        mockMvc.perform(post("/api/compliance/insurance-policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "policyType": "RCTR_C",
                      "insurer": "Example Seguros",
                      "policyNumberLast4": "6789",
                      "startsOn": "%s",
                      "expiresOn": "%s",
                      "annualPremium": "3600.00",
                      "monthlyReserve": "300.00",
                      "currency": "BRL",
                      "linkedRntrc": true,
                      "pgrRequired": true,
                      "verificationStatus": "VERIFIED_BY_DRIVER"
                    }
                    """.formatted(expiresOn.minusYears(1), expiresOn)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.insurancePolicy.policyNumberMasked").value("***6789"))
            .andExpect(jsonPath("$.insurancePolicy.status").value("EXPIRING_SOON"))
            .andExpect(jsonPath("$.insurancePolicy.advisoryText").value(
                "Insurance data is organization metadata. Confirm coverage, obligations, and policy validity with the insurer, SUSEP, or a qualified professional."));

        mockMvc.perform(get("/api/compliance/insurance-policies").queryParam("active", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.insurancePolicies", hasSize(1)));

        mockMvc.perform(get("/api/compliance/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calendarItems", hasSize(1)))
            .andExpect(jsonPath("$.calendarItems[0].subjectType").value("INSURANCE_POLICY"))
            .andExpect(jsonPath("$.calendarItems[0].status").value("EXPIRING_SOON"))
            .andExpect(jsonPath("$.calendarItems[0].severity").value("WARNING"));
    }

    @Test
    @WithMockUser(username = "compliance-documents@example.test")
    void savesDocumentsAndIncludesExpirationAlertsInProfileAndScore() throws Exception {
        createDriver("Compliance Documents", "42345678").andExpect(status().isCreated());
        var expiresOn = LocalDate.now().minusDays(1);

        mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "documentType": "CRLV",
                      "ownerType": "DRIVER",
                      "title": "CRLV 2026",
                      "identifierLast4": "1122",
                      "issuedOn": "%s",
                      "expiresOn": "%s",
                      "source": "DRIVER_ENTERED",
                      "notes": "Uploaded for internal organization only"
                    }
                    """.formatted(expiresOn.minusYears(1), expiresOn)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.document.identifierMasked").value("***1122"))
            .andExpect(jsonPath("$.document.status").value("EXPIRED"))
            .andExpect(jsonPath("$.document.advisoryText").value(
                "Document reminders are advisory organization aids and do not certify legal, tax, insurance, or government compliance."));

        mockMvc.perform(get("/api/documents").queryParam("type", "CRLV"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documents", hasSize(1)));

        mockMvc.perform(get("/api/compliance/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complianceProfile.documents.expired").value(1))
            .andExpect(jsonPath("$.complianceProfile.alerts[*].alertType", hasItem("DOCUMENT_EXPIRATION")))
            .andExpect(jsonPath("$.complianceProfile.score.score.status").value("RISK"));

        mockMvc.perform(get("/api/compliance/score"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complianceScore.score.components[*].name", hasItem("documents")))
            .andExpect(jsonPath("$.complianceScore.caveats", hasItem(
                "Cofrete is not an official government, legal, tax, accounting, or insurance channel.")));
    }

    @Test
    @WithMockUser(username = "waiting-time-rule@example.test")
    void upsertsWaitingTimeRuleAndReturnsMissingOrStaleAdvisoryStatus() throws Exception {
        mockMvc.perform(get("/api/source-rules/waiting-time")
                .queryParam("effectiveDate", "2026-05-14"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.waitingTimeRule.ruleStatus").value("MISSING"))
            .andExpect(jsonPath("$.waitingTimeRule.advisoryText").value(
                "No source-backed waiting-time rule is configured for this date. Keep customer-charge guidance conservative."));

        mockMvc.perform(post("/api/source-rules/waiting-time")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "thresholdHours": "5.00",
                      "ratePerTonHour": "2.34",
                      "currency": "BRL",
                      "effectiveFrom": "2026-01-01",
                      "sourceUrl": "https://www.gov.br/transportes",
                      "reviewedAt": "2024-01-10T00:00:00Z",
                      "freshnessStatus": "CURRENT",
                      "confidence": "MEDIUM"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.waitingTimeRule.thresholdHours").value("5.00"))
            .andExpect(jsonPath("$.waitingTimeRule.ratePerTonHour").value("2.34"))
            .andExpect(jsonPath("$.waitingTimeRule.ruleStatus").value("STALE"))
            .andExpect(jsonPath("$.waitingTimeRule.advisoryText").value(
                "Waiting-time impact is an advisory estimate. Confirm official rules and contract terms before charging or disputing a customer."));
    }

    private org.springframework.test.web.servlet.ResultActions createDriver(String name, String rntrcNumber) throws Exception {
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
                  "rntrcNumber": "%s",
                  "rntrcCategory": "TAC",
                  "rntrcStatus": "UNKNOWN",
                  "active": true
                }
                """.formatted(name, rntrcNumber)));
    }
}
