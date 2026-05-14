package com.cofrete.coreapi.trip;

import java.time.Instant;

record TripRecalculationRequestedEvent(
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

    static TripRecalculationRequestedEvent from(TripRecalculationEventRecord record) {
        return new TripRecalculationRequestedEvent(
            record.getEventType(),
            record.getVersion(),
            record.getEventId(),
            record.getIdempotencyKey(),
            record.getTripId(),
            record.getDriverId(),
            record.getTruckId(),
            record.getInputRevision(),
            record.getReason().name(),
            record.getCorrelationId(),
            record.getProducer(),
            record.getRequestedAt()
        );
    }
}
