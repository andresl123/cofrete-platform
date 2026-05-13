package com.cofrete.coreapi.reserve;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class ReserveEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticatedUserService authenticatedUsers;

    @Autowired
    private ReserveRuleRepository reserveRules;

    @Test
    void reserveApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/reserve-wallets"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "reserve-allocation@example.test")
    void createsRulesAllocatesWalletsAndExposesTransactionHistory() throws Exception {
        createPercentRule("MAINTENANCE", "0.080000", "5000.00")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveRule.targetBalance").value("5000.00"));
        createPercentRule("TIRES", "0.040000", "3000.00").andExpect(status().isCreated());
        createPercentRule("TAXES_AND_DOCUMENTS", "0.030000", "2000.00").andExpect(status().isCreated());
        createPercentRule("DRIVER_SALARY", "0.150000", null).andExpect(status().isCreated());
        createPercentRule("PROFIT", "0.050000", null).andExpect(status().isCreated());

        allocate("reserve-trip_123-pay_123-v1", "8000.00", "600.00")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveAllocation.status").value("ALLOCATED"))
            .andExpect(jsonPath("$.reserveAllocation.requestStatus").value("ALLOCATED"))
            .andExpect(jsonPath("$.reserveAllocation.duplicate").value(false))
            .andExpect(jsonPath("$.reserveAllocation.allocatableAmount").value("7400.00"))
            .andExpect(jsonPath("$.reserveAllocation.requiredReserveAmount").value("1110.00"))
            .andExpect(jsonPath("$.reserveAllocation.safePersonalWithdrawal").value("1110.00"))
            .andExpect(jsonPath("$.reserveAllocation.bucketAllocations.MAINTENANCE").value("592.00"))
            .andExpect(jsonPath("$.reserveAllocation.bucketAllocations.DRIVER_SALARY").value("1110.00"))
            .andExpect(jsonPath("$.reserveAllocation.transactions", hasSize(5)));

        allocate("reserve-trip_123-pay_123-v1", "8000.00", "600.00")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveAllocation.status").value("ALLOCATED"))
            .andExpect(jsonPath("$.reserveAllocation.requestStatus").value("DUPLICATE_IGNORED"))
            .andExpect(jsonPath("$.reserveAllocation.duplicate").value(true))
            .andExpect(jsonPath("$.reserveAllocation.transactions", hasSize(5)));

        mockMvc.perform(get("/api/reserve-wallets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reserveWallets", hasSize(5)))
            .andExpect(jsonPath("$.reserveWallets[?(@.bucket=='MAINTENANCE')].currentBalance").value("592.00"))
            .andExpect(jsonPath("$.reserveWallets[?(@.bucket=='MAINTENANCE')].transactions[0].amount").value("592.00"));
    }

    @Test
    @WithMockUser(username = "reserve-idempotency-conflict@example.test")
    void rejectsSameIdempotencyKeyWithDifferentInputs() throws Exception {
        createPercentRule("MAINTENANCE", "0.100000", "5000.00").andExpect(status().isCreated());
        allocate("reserve-conflict-key", "1000.00", "0.00").andExpect(status().isCreated());

        allocate("reserve-conflict-key", "1100.00", "0.00")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @WithMockUser(username = "reserve-idempotency-scale@example.test")
    void treatsEquivalentMoneyScalesAsSameIdempotencyInput() throws Exception {
        createPercentRule("MAINTENANCE", "0.100000", "5000.00").andExpect(status().isCreated());

        allocate("reserve-scale-key", "1000.0", "0.0")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveAllocation.requestStatus").value("ALLOCATED"));

        allocate("reserve-scale-key", "1000.00", "0.00")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveAllocation.requestStatus").value("DUPLICATE_IGNORED"));
    }

    @Test
    @WithMockUser(username = "reserve-health@example.test")
    void returnsFinancialHealthScoreFromWalletCoverage() throws Exception {
        createPercentRule("MAINTENANCE", "0.500000", "500.00").andExpect(status().isCreated());
        createPercentRule("DRIVER_SALARY", "0.250000", null).andExpect(status().isCreated());
        allocate("reserve-health-v1", "1000.00", "0.00").andExpect(status().isCreated());

        mockMvc.perform(get("/api/financial-health-score"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.financialHealthScore.score").value(100))
            .andExpect(jsonPath("$.financialHealthScore.status").value("GOOD"))
            .andExpect(jsonPath("$.financialHealthScore.safePersonalWithdrawalAvailable").value("250.00"))
            .andExpect(jsonPath("$.financialHealthScore.components", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "reserve-validation@example.test")
    void validatesRulesAndAllocations() throws Exception {
        mockMvc.perform(post("/api/reserve-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bucket": "MAINTENANCE",
                      "policy": "PERCENT_OF_AMOUNT",
                      "currency": "BRL"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/reserve-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bucket": "TIRES",
                      "policy": "PERCENT_OF_AMOUNT",
                      "rate": "0.040000",
                      "fixedAmount": "25.00",
                      "currency": "BRL"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/reserve-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bucket": "EMERGENCY",
                      "policy": "FIXED_AMOUNT",
                      "fixedAmount": "25.001",
                      "currency": "BRL"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        allocate("reserve-no-rules", "100.00", "0.00")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/reserve-allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "allocationRevision": 1,
                      "grossAmount": "100.001",
                      "passThroughAmount": "0.00",
                      "currency": "BRL",
                      "idempotencyKey": "reserve-invalid-scale",
                      "reason": "FREIGHT_PAYMENT_RECEIVED",
                      "requestedAt": "2026-05-11T12:00:10Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/reserve-allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "allocationRevision": 1,
                      "grossAmount": "100.00",
                      "passThroughAmount": "0.00",
                      "currency": "BRL",
                      "idempotencyKey": "reserve-missing-subject",
                      "reason": "FREIGHT_PAYMENT_RECEIVED",
                      "requestedAt": "2026-05-11T12:00:10Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "reserve-per-km@example.test")
    void requiresDistanceForPerKmRules() throws Exception {
        mockMvc.perform(post("/api/reserve-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bucket": "MAINTENANCE",
                      "policy": "PER_KM",
                      "perKmAmount": "0.2500",
                      "currency": "BRL"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveRule.perKmAmount").value("0.2500"));

        allocate("reserve-per-km-no-distance", "1000.00", "0.00")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void databasePreventsConcurrentActiveRulesForSameAccountBucket() {
        var authentication = new UsernamePasswordAuthenticationToken(
            "reserve-db-unique@example.test",
            "n/a",
            List.of()
        );
        var user = authenticatedUsers.requireUser(authentication);
        var firstRule = new ReserveRule(user.getAccountId(), new ReserveRuleRequest(
            ReserveBucket.MAINTENANCE,
            ReserveRulePolicy.PERCENT_OF_AMOUNT,
            new BigDecimal("0.100000"),
            null,
            null,
            new BigDecimal("5000.00"),
            "BRL",
            null,
            null,
            true,
            "first active rule"
        ));
        var duplicateRule = new ReserveRule(user.getAccountId(), new ReserveRuleRequest(
            ReserveBucket.MAINTENANCE,
            ReserveRulePolicy.PERCENT_OF_AMOUNT,
            new BigDecimal("0.200000"),
            null,
            null,
            new BigDecimal("6000.00"),
            "BRL",
            null,
            null,
            true,
            "duplicate active rule"
        ));

        reserveRules.saveAndFlush(firstRule);

        assertThrows(
            DataIntegrityViolationException.class,
            () -> reserveRules.saveAndFlush(duplicateRule)
        );
    }

    @Test
    @WithMockUser(username = "reserve-manual-correction@example.test")
    void manualCorrectionCreatesAuditableCreditTransaction() throws Exception {
        createPercentRule("EMERGENCY", "0.100000", "1000.00").andExpect(status().isCreated());

        mockMvc.perform(post("/api/reserve-allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "allocationSubjectId": "manual_123",
                      "allocationRevision": 1,
                      "grossAmount": "500.00",
                      "passThroughAmount": "0.00",
                      "currency": "BRL",
                      "idempotencyKey": "manual-emergency-123-v1",
                      "reason": "MANUAL_CORRECTION",
                      "requestedAt": "2026-05-11T12:00:10Z"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reserveAllocation.transactions[0].type").value("CREDIT"))
            .andExpect(jsonPath("$.reserveAllocation.transactions[0].sourceReference").value("manual-emergency-123-v1"));
    }

    private org.springframework.test.web.servlet.ResultActions createPercentRule(
        String bucket,
        String rate,
        String targetBalance
    ) throws Exception {
        String targetField = targetBalance == null ? "" : "\"targetBalance\": \"" + targetBalance + "\",";
        return mockMvc.perform(post("/api/reserve-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "bucket": "%s",
                  "policy": "PERCENT_OF_AMOUNT",
                  "rate": "%s",
                  %s
                  "currency": "BRL",
                  "sourceAssumption": "starter MVP rule"
                }
                """.formatted(bucket, rate, targetField)));
    }

    private org.springframework.test.web.servlet.ResultActions allocate(
        String idempotencyKey,
        String grossAmount,
        String passThroughAmount
    ) throws Exception {
        return mockMvc.perform(post("/api/reserve-allocations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "tripId": "trip_123",
                  "freightPaymentId": "pay_123",
                  "allocationSubjectId": "pay_123",
                  "allocationRevision": 1,
                  "grossAmount": "%s",
                  "passThroughAmount": "%s",
                  "currency": "BRL",
                  "idempotencyKey": "%s",
                  "reason": "FREIGHT_PAYMENT_RECEIVED",
                  "requestedAt": "2026-05-11T12:00:10Z"
                }
                """.formatted(grossAmount, passThroughAmount, idempotencyKey)));
    }
}
