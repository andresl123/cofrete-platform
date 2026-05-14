package com.cofrete.coreapi.trip;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TripRecalculationEventPublisherTests {

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishesRabbitEventAfterTransactionCommit() {
        var repository = mock(TripRecalculationEventRepository.class);
        when(repository.save(any())).then(returnsFirstArg());
        var rabbitTemplate = mock(RabbitTemplate.class);
        var properties = enabledProperties();
        var publisher = new JpaTripRecalculationEventPublisher(repository, rabbitTemplate, properties);

        TransactionSynchronizationManager.initSynchronization();
        publisher.publish(trip(), RecalculationReason.TRIP_CREATED, "corr_test");

        verifyNoInteractions(rabbitTemplate);

        TransactionSynchronizationManager.getSynchronizations()
            .forEach(synchronization -> synchronization.afterCommit());

        var eventCaptor = org.mockito.ArgumentCaptor.forClass(TripRecalculationRequestedEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq("cofrete.finance.events"),
            eq("trip.recalculation.requested"),
            eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().eventType()).isEqualTo("trip.recalculation.requested");
        assertThat(eventCaptor.getValue().version()).isEqualTo(1);
        assertThat(eventCaptor.getValue().eventId()).startsWith("evt_");
        assertThat(eventCaptor.getValue().idempotencyKey()).isEqualTo("trip:trip_123:recalculation:3");
        assertThat(eventCaptor.getValue().tripId()).isEqualTo("trip_123");
        assertThat(eventCaptor.getValue().driverId()).isEqualTo("driver_123");
        assertThat(eventCaptor.getValue().truckId()).isEqualTo("truck_123");
        assertThat(eventCaptor.getValue().inputRevision()).isEqualTo(3);
        assertThat(eventCaptor.getValue().reason()).isEqualTo("TRIP_CREATED");
        assertThat(eventCaptor.getValue().correlationId()).isEqualTo("corr_test");
        assertThat(eventCaptor.getValue().producer()).isEqualTo("core-api");
        assertThat(eventCaptor.getValue().requestedAt()).isNotNull();
    }

    @Test
    void publishesImmediatelyWhenNoTransactionSynchronizationExists() {
        var repository = mock(TripRecalculationEventRepository.class);
        when(repository.save(any())).then(returnsFirstArg());
        var rabbitTemplate = mock(RabbitTemplate.class);
        var properties = enabledProperties();
        var publisher = new JpaTripRecalculationEventPublisher(repository, rabbitTemplate, properties);

        publisher.publish(trip(), RecalculationReason.TRIP_CREATED, "corr_test");

        verify(rabbitTemplate).convertAndSend(
            eq("cofrete.finance.events"),
            eq("trip.recalculation.requested"),
            any(TripRecalculationRequestedEvent.class)
        );
    }

    private static CoreApiRabbitProperties enabledProperties() {
        var properties = new CoreApiRabbitProperties();
        properties.setEnabled(true);
        properties.setExchange("cofrete.finance.events");
        properties.setTripRecalculationRequestedRoutingKey("trip.recalculation.requested");
        return properties;
    }

    private static Trip trip() {
        var trip = mock(Trip.class);
        when(trip.getId()).thenReturn("trip_123");
        when(trip.getDriverId()).thenReturn("driver_123");
        when(trip.getTruckId()).thenReturn("truck_123");
        when(trip.getInputRevision()).thenReturn(3);
        return trip;
    }
}
