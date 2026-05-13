package com.cofrete.financeworker.messaging.events;

import java.time.Instant;

public record TripRecalculationRequestedEvent(
    String eventType,
    int version,
    String eventId,
    String tripId,
    String driverId,
    String truckId,
    String reason,
    String correlationId,
    Instant requestedAt
) {
}
