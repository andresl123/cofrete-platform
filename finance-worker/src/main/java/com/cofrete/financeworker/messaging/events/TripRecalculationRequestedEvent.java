package com.cofrete.financeworker.messaging.events;

import java.time.Instant;

public record TripRecalculationRequestedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String tripId,
    String driverId,
    String truckId,
    int inputRevision,
    String reason,
    String correlationId,
    String producer,
    Instant requestedAt
) {
}
