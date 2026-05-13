package com.cofrete.financeworker.messaging.events;

import java.time.Instant;

public record ReserveAllocationRequestedEvent(
    String eventType,
    int version,
    String eventId,
    String freightPaymentId,
    String tripId,
    String grossAmount,
    String currency,
    String correlationId,
    Instant requestedAt
) {
}
