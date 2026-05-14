package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.trip.CoreApiRabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class RabbitReserveAllocationEventPublisher implements ReserveAllocationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final CoreApiRabbitProperties properties;

    RabbitReserveAllocationEventPublisher(RabbitTemplate rabbitTemplate, CoreApiRabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(ReserveAllocationRequestedEvent event) {
        if (!properties.isEnabled()) {
            return;
        }
        publishAfterCommit(event);
    }

    private void publishAfterCommit(ReserveAllocationRequestedEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publishToRabbit(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishToRabbit(event);
            }
        });
    }

    private void publishToRabbit(ReserveAllocationRequestedEvent event) {
        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getReserveAllocationRequestedRoutingKey(),
            event
        );
    }
}
