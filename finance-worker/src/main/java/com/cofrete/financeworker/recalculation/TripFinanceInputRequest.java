package com.cofrete.financeworker.recalculation;

import java.time.Instant;

public record TripFinanceInputRequest(
    String eventId,
    String idempotencyKey,
    String tripId,
    String driverId,
    String truckId,
    int inputRevision,
    String reason,
    String correlationId,
    Instant requestedAt
) {
}
