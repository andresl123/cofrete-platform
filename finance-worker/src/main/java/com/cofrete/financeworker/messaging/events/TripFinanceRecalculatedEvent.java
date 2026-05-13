package com.cofrete.financeworker.messaging.events;

import java.time.Instant;

public record TripFinanceRecalculatedEvent(
    String eventType,
    int version,
    String eventId,
    String tripId,
    String grossFreight,
    String directTripCost,
    String requiredReserves,
    String safePersonalWithdrawal,
    String currency,
    String financialHealthImpact,
    String correlationId,
    Instant occurredAt
) {
}
