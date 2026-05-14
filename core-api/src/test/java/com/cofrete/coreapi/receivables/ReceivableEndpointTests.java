package com.cofrete.coreapi.receivables;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class ReceivableEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReceivableRepository receivables;

    @Autowired
    private ReceivableStatusChangeRepository statusChanges;

    @Test
    void receivableApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/receivables"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "receivable-lifecycle@example.test")
    void createsReceivableFiltersOverdueAndMarksPartialThenPaidWithAuditTrail() throws Exception {
        String customerId = createCustomer("Cliente Alfa");
        String receivableId = createReceivable(
            customerId,
            "BALANCE",
            "5000.00",
            LocalDate.now().minusDays(5),
            "Balance after delivery."
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.receivable.id", startsWith("recv_")))
            .andExpect(jsonPath("$.receivable.status").value("EXPECTED"))
            .andExpect(jsonPath("$.receivable.remainingAmount").value("5000.00"))
            .andExpect(jsonPath("$.receivable.statusChanges", hasSize(1)))
            .andReturn()
            .getResponse()
            .getContentAsString();
        receivableId = objectMapper.readTree(receivableId).path("receivable").path("id").asText();

        mockMvc.perform(get("/api/receivables").param("status", "overdue"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receivables", hasSize(1)))
            .andExpect(jsonPath("$.receivables[0].status").value("LATE"))
            .andExpect(jsonPath("$.receivables[0].statusChanges", hasSize(2)))
            .andExpect(jsonPath("$.totals.lateAmount").value("5000.00"));

        markPaid(receivableId, "2000.00", LocalDate.now().minusDays(2), "Partial Pix")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receivable.status").value("PARTIALLY_PAID"))
            .andExpect(jsonPath("$.receivable.paidAmount").value("2000.00"))
            .andExpect(jsonPath("$.receivable.remainingAmount").value("3000.00"))
            .andExpect(jsonPath("$.receivable.payments", hasSize(1)))
            .andExpect(jsonPath("$.receivable.statusChanges", hasSize(3)));

        markPaid(receivableId, "3000.00", LocalDate.now().minusDays(1), "Final Pix")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receivable.status").value("PAID"))
            .andExpect(jsonPath("$.receivable.remainingAmount").value("0.00"))
            .andExpect(jsonPath("$.receivable.payments", hasSize(2)))
            .andExpect(jsonPath("$.receivable.statusChanges", hasSize(4)));

        var saved = receivables.findById(receivableId).orElseThrow();
        assertThat(statusChanges.findByReceivableOrderByChangedAtAsc(saved))
            .extracting(ReceivableStatusChange::getNewStatus)
            .containsExactly(
                ReceivableStatus.EXPECTED,
                ReceivableStatus.LATE,
                ReceivableStatus.PARTIALLY_PAID,
                ReceivableStatus.PAID
            );
    }

    @Test
    @WithMockUser(username = "receivable-profitability@example.test")
    void customerProfitabilityIncludesDelayAndReceivableRiskInputs() throws Exception {
        String customerId = createCustomer("Cliente Beta");
        String paid = idFrom(createReceivable(
            customerId,
            "BALANCE",
            "3000.00",
            LocalDate.now().minusDays(6),
            "Paid late"
        ).andReturn(), "receivable");
        markPaid(paid, "3000.00", LocalDate.now().minusDays(2), "Paid after due date")
            .andExpect(status().isOk());

        createReceivable(
            customerId,
            "REIMBURSEMENT",
            "700.00",
            LocalDate.now().minusDays(3),
            "Unpaid reimbursement"
        ).andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers/{customerId}/profitability", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerProfitability.customerId").value(customerId))
            .andExpect(jsonPath("$.customerProfitability.grossFreight").value("3700.00"))
            .andExpect(jsonPath("$.customerProfitability.expectedProfit").value("0.00"))
            .andExpect(jsonPath("$.customerProfitability.averagePaymentDelayDays").value("4.00"))
            .andExpect(jsonPath("$.customerProfitability.lateReceivables").value("700.00"))
            .andExpect(jsonPath("$.customerProfitability.receivableRiskStatus").value("HIGH"))
            .andExpect(jsonPath("$.customerProfitability.receivableRiskInputs.receivableCount").value(2))
            .andExpect(jsonPath("$.customerProfitability.receivableRiskInputs.paidReceivableCount").value(1))
            .andExpect(jsonPath("$.customerProfitability.receivableRiskInputs.lateReceivableCount").value(1))
            .andExpect(jsonPath("$.customerProfitability.qualitySignals.paymentDelayStatus").value("HIGH"));
    }

    @Test
    @WithMockUser(username = "receivable-validation@example.test")
    void validatesCustomersReceivablesAndPayments() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Cliente Sem Tipo",
                      "taxIdLast4": "1234",
                      "paymentTermsDays": 21
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        String customerId = createCustomer("Cliente Gama");

        mockMvc.perform(post("/api/receivables")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerId": "%s",
                      "type": "BALANCE",
                      "amount": "100.001",
                      "currency": "BRL",
                      "dueDate": "%s",
                      "paymentMethod": "PIX"
                    }
                    """.formatted(customerId, LocalDate.now().plusDays(5))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        String receivableId = idFrom(createReceivable(
            customerId,
            "BALANCE",
            "100.00",
            LocalDate.now().plusDays(5),
            "Open balance"
        ).andReturn(), "receivable");

        markPaid(receivableId, "150.00", LocalDate.now(), "Too much")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/receivables").param("status", "unknown-status"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private String createCustomer(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "taxIdType": "CNPJ",
                      "taxIdLast4": "1234",
                      "contactName": "Financeiro",
                      "contactPhone": "+55 62 99999-0000",
                      "paymentTermsDays": 21,
                      "notes": "MVP customer profile"
                    }
                    """.formatted(name)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customer.id", startsWith("cust_")))
            .andReturn();
        return idFrom(result, "customer");
    }

    private org.springframework.test.web.servlet.ResultActions createReceivable(
        String customerId,
        String type,
        String amount,
        LocalDate dueDate,
        String note
    ) throws Exception {
        return mockMvc.perform(post("/api/receivables")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": "%s",
                  "tripId": "trip_receivable_123",
                  "type": "%s",
                  "amount": "%s",
                  "currency": "BRL",
                  "dueDate": "%s",
                  "paymentMethod": "PIX",
                  "note": "%s"
                }
                """.formatted(customerId, type, amount, dueDate, note)));
    }

    private org.springframework.test.web.servlet.ResultActions markPaid(
        String receivableId,
        String amount,
        LocalDate paidDate,
        String note
    ) throws Exception {
        return mockMvc.perform(put("/api/receivables/{receivableId}/mark-paid", receivableId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "paidAmount": "%s",
                  "currency": "BRL",
                  "paidDate": "%s",
                  "paymentMethod": "PIX",
                  "note": "%s"
                }
                """.formatted(amount, paidDate, note)));
    }

    private String idFrom(MvcResult result, String envelopeName) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path(envelopeName).path("id").asText();
    }
}
