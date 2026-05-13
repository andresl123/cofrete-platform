package com.cofrete.coreapi.reserve;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

record ReserveRuleEnvelope(ReserveRuleResponse reserveRule) {
}

record ReserveWalletsEnvelope(List<ReserveWalletResponse> reserveWallets) {
}

record ReserveAllocationEnvelope(ReserveAllocationResponse reserveAllocation) {
}

record FinancialHealthEnvelope(FinancialHealthResponse financialHealthScore) {
}

record ReserveRuleResponse(
    String id,
    ReserveBucket bucket,
    ReserveRulePolicy policy,
    String rate,
    String fixedAmount,
    String perKmAmount,
    String targetBalance,
    String currency,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    boolean active,
    String sourceAssumption,
    Instant createdAt,
    Instant updatedAt
) {

    static ReserveRuleResponse from(ReserveRule rule) {
        return new ReserveRuleResponse(
            rule.getId(),
            rule.getBucket(),
            rule.getPolicy(),
            ReserveMoney.optional(rule.getRate()),
            ReserveMoney.optionalMoney(rule.getFixedAmount()),
            ReserveMoney.optionalPerKm(rule.getPerKmAmount()),
            ReserveMoney.optionalMoney(rule.getTargetBalance()),
            rule.getCurrency(),
            rule.getEffectiveFrom(),
            rule.getEffectiveTo(),
            rule.isActive(),
            rule.getSourceAssumption(),
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }
}

record ReserveWalletResponse(
    ReserveBucket bucket,
    String currentBalance,
    String targetBalance,
    String currency,
    ReserveRulePolicy policy,
    Instant lastAllocationAt,
    List<ReserveTransactionResponse> transactions
) {

    static ReserveWalletResponse from(
        ReserveWallet wallet,
        ReserveRule rule,
        List<ReserveTransaction> transactions
    ) {
        return new ReserveWalletResponse(
            wallet.getBucket(),
            ReserveMoney.money(wallet.getCurrentBalance()),
            ReserveMoney.optionalMoney(wallet.getTargetBalance()),
            wallet.getCurrency(),
            rule == null ? null : rule.getPolicy(),
            wallet.getLastAllocationAt(),
            transactions.stream().map(ReserveTransactionResponse::from).toList()
        );
    }
}

record ReserveTransactionResponse(
    String id,
    ReserveBucket bucket,
    ReserveTransactionType type,
    String amount,
    String balanceAfter,
    String currency,
    String sourceType,
    String sourceReference,
    String note,
    Instant createdAt
) {

    static ReserveTransactionResponse from(ReserveTransaction transaction) {
        return new ReserveTransactionResponse(
            transaction.getId(),
            transaction.getBucket(),
            transaction.getTransactionType(),
            ReserveMoney.money(transaction.getAmount()),
            ReserveMoney.money(transaction.getBalanceAfter()),
            transaction.getCurrency(),
            transaction.getSourceType(),
            transaction.getSourceReference(),
            transaction.getNote(),
            transaction.getCreatedAt()
        );
    }
}

record ReserveAllocationResponse(
    String id,
    ReserveAllocationStatus status,
    ReserveAllocationRequestStatus requestStatus,
    boolean duplicate,
    String idempotencyKey,
    String grossAmount,
    String passThroughAmount,
    String allocatableAmount,
    String requiredReserveAmount,
    String safePersonalWithdrawal,
    String currency,
    ReserveAllocationReason reason,
    Instant requestedAt,
    Map<ReserveBucket, String> bucketAllocations,
    List<ReserveTransactionResponse> transactions,
    String advisoryText
) {

    static ReserveAllocationResponse from(
        ReserveAllocation allocation,
        ReserveAllocationRequestStatus requestStatus,
        Map<ReserveBucket, String> bucketAllocations,
        List<ReserveTransaction> transactions
    ) {
        return new ReserveAllocationResponse(
            allocation.getId(),
            allocation.getStatus(),
            requestStatus,
            requestStatus == ReserveAllocationRequestStatus.DUPLICATE_IGNORED,
            allocation.getIdempotencyKey(),
            ReserveMoney.money(allocation.getGrossAmount()),
            ReserveMoney.money(allocation.getPassThroughAmount()),
            ReserveMoney.money(allocation.getAllocatableAmount()),
            ReserveMoney.money(allocation.getRequiredReserveAmount()),
            ReserveMoney.money(allocation.getSafePersonalWithdrawal()),
            allocation.getCurrency(),
            allocation.getReason(),
            allocation.getRequestedAt(),
            bucketAllocations,
            transactions.stream().map(ReserveTransactionResponse::from).toList(),
            "Reserve allocations are virtual ledger movements, not real money transfers or legal/accounting advice."
        );
    }
}

record FinancialHealthResponse(
    int score,
    FinancialHealthStatus status,
    String reserveCoveragePercent,
    String safePersonalWithdrawalAvailable,
    String currency,
    String traceId,
    List<FinancialHealthComponentResponse> components,
    String advisoryText
) {
}

record FinancialHealthComponentResponse(
    ReserveBucket bucket,
    String currentBalance,
    String targetBalance,
    String coveragePercent,
    FinancialHealthStatus status
) {
}
