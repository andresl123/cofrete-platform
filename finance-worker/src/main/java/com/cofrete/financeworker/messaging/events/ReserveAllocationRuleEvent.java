package com.cofrete.financeworker.messaging.events;

public record ReserveAllocationRuleEvent(
    String bucket,
    String policy,
    String rate,
    String fixedAmount,
    String perKmAmount
) {
}
