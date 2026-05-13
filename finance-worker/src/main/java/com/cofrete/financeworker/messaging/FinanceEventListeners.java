package com.cofrete.financeworker.messaging;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FinanceEventListeners {

    @RabbitListener(queues = "${cofrete.finance-worker.rabbitmq.trip-recalculation-queue}")
    public void handleTripRecalculationRequested(TripRecalculationRequestedEvent event) {
        assertEventType(event.eventType(), FinanceEventNames.TRIP_RECALCULATION_REQUESTED);
        // ROU-216 owns deterministic finance recalculation implementation.
    }

    @RabbitListener(queues = "${cofrete.finance-worker.rabbitmq.reserve-allocation-queue}")
    public void handleReserveAllocationRequested(ReserveAllocationRequestedEvent event) {
        assertEventType(event.eventType(), FinanceEventNames.RESERVE_ALLOCATION_REQUESTED);
        // ROU-217 owns deterministic reserve allocation implementation.
    }

    private static void assertEventType(String actual, String expected) {
        Assert.isTrue(expected.equals(actual), () -> "Expected eventType " + expected + " but received " + actual);
    }
}
