package com.cofrete.coreapi.reserve;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class ReserveAllocationResultListener {

    private final ReserveService reserves;

    ReserveAllocationResultListener(ReserveService reserves) {
        this.reserves = reserves;
    }

    @RabbitListener(queues = "${cofrete.core-api.rabbitmq.reserve-allocation-completed-queue}")
    void handleReserveAllocationCompleted(ReserveAllocationCompletedEvent event) {
        reserves.persistWorkerResult(event);
    }
}
