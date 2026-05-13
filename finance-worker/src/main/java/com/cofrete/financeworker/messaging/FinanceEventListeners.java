package com.cofrete.financeworker.messaging;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import com.cofrete.financeworker.recalculation.TripFinanceRecalculationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FinanceEventListeners {

    private final TripFinanceRecalculationService tripFinanceRecalculationService;

    public FinanceEventListeners(TripFinanceRecalculationService tripFinanceRecalculationService) {
        this.tripFinanceRecalculationService = tripFinanceRecalculationService;
    }

    @RabbitListener(queues = "${cofrete.finance-worker.rabbitmq.trip-recalculation-queue}")
    public void handleTripRecalculationRequested(TripRecalculationRequestedEvent event) {
        tripFinanceRecalculationService.handle(event);
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
