package com.cofrete.financeworker.reserve;

public record ReserveAllocationRule(
    ReserveBucket bucket,
    ReserveRulePolicy policy,
    String rate,
    String fixedAmount,
    String perKmAmount
) {
}
