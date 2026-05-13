package com.cofrete.financeworker.messaging.events;

import java.time.Instant;

public record ReserveAllocationRequestedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String allocationSubjectId,
    int allocationRevision,
    String freightPaymentId,
    String tripId,
    String driverId,
    String grossAmount,
    String passThroughAmount,
    String currency,
    String reason,
    String correlationId,
    String producer,
    Instant requestedAt
) {
}
