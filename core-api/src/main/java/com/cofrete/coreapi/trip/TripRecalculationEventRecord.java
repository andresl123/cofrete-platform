package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "trip_recalculation_events")
class TripRecalculationEventRecord {

    static final String EVENT_TYPE = "trip.recalculation.requested";
    static final int VERSION = 1;
    static final String PRODUCER = "core-api";

    @Id
    private String id;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, unique = true, length = 80)
    private String eventId;

    @Column(nullable = false, unique = true, length = 160)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String tripId;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(nullable = false, length = 64)
    private String truckId;

    @Column(nullable = false)
    private int inputRevision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private RecalculationReason reason;

    @Column(nullable = false, length = 160)
    private String correlationId;

    @Column(nullable = false, length = 80)
    private String producer;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = false)
    private Instant publishedAt;

    protected TripRecalculationEventRecord() {
    }

    TripRecalculationEventRecord(Trip trip, RecalculationReason reason, String correlationId) {
        id = DomainIds.prefixed("trip_event");
        eventType = EVENT_TYPE;
        version = VERSION;
        tripId = trip.getId();
        driverId = trip.getDriverId();
        truckId = trip.getTruckId();
        inputRevision = trip.getInputRevision();
        this.reason = reason;
        this.correlationId = correlationId;
        producer = PRODUCER;
        idempotencyKey = "trip:" + tripId + ":recalculation:" + inputRevision;
        eventId = DomainIds.prefixed("evt");
        requestedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        publishedAt = Instant.now();
    }

    String getEventType() {
        return eventType;
    }

    int getVersion() {
        return version;
    }

    String getEventId() {
        return eventId;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    String getTripId() {
        return tripId;
    }

    String getDriverId() {
        return driverId;
    }

    String getTruckId() {
        return truckId;
    }

    int getInputRevision() {
        return inputRevision;
    }

    RecalculationReason getReason() {
        return reason;
    }

    String getCorrelationId() {
        return correlationId;
    }

    String getProducer() {
        return producer;
    }

    Instant getRequestedAt() {
        return requestedAt;
    }
}
