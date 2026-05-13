package com.cofrete.financeworker.messaging.events;

import java.time.Instant;
import java.util.Map;

public record TripFinanceRecalculatedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String tripId,
    String driverId,
    int inputRevision,
    String calculationTraceId,
    String grossFreight,
    String passThroughAmount,
    String directTripCost,
    String requiredReserves,
    String safePersonalWithdrawal,
    String expectedProfit,
    String currency,
    String financialHealthImpact,
    Map<String, String> sourceFreshness,
    String correlationId,
    String producer,
    Instant calculatedAt
) {
}
