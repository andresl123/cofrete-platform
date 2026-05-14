package com.cofrete.coreapi.trip;

interface TripRecalculationEventPublisher {

    TripRecalculationEventRecord publish(Trip trip, RecalculationReason reason, String correlationId);
}
