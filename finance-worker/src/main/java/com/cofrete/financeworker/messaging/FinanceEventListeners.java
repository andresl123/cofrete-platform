package com.cofrete.financeworker.messaging;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import com.cofrete.financeworker.recalculation.TripFinanceRecalculationService;
import com.cofrete.financeworker.reserve.ReserveAllocationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FinanceEventListeners {

    private final TripFinanceRecalculationService tripFinanceRecalculationService;
    private final ReserveAllocationService reserveAllocationService;

    public FinanceEventListeners(
        TripFinanceRecalculationService tripFinanceRecalculationService,
        ReserveAllocationService reserveAllocationService
    ) {
        this.tripFinanceRecalculationService = tripFinanceRecalculationService;
        this.reserveAllocationService = reserveAllocationService;
    }

    @RabbitListener(queues = "${cofrete.finance-worker.rabbitmq.trip-recalculation-queue}")
    public void handleTripRecalculationRequested(TripRecalculationRequestedEvent event) {
        tripFinanceRecalculationService.handle(event);
    }

    @RabbitListener(queues = "${cofrete.finance-worker.rabbitmq.reserve-allocation-queue}")
    public void handleReserveAllocationRequested(ReserveAllocationRequestedEvent event) {
        reserveAllocationService.handle(event);
    }
}
