package com.cofrete.coreapi.trip;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class JpaTripRecalculationEventPublisher implements TripRecalculationEventPublisher {

    private final TripRecalculationEventRepository events;
    private final RabbitTemplate rabbitTemplate;
    private final CoreApiRabbitProperties properties;

    JpaTripRecalculationEventPublisher(
        TripRecalculationEventRepository events,
        RabbitTemplate rabbitTemplate,
        CoreApiRabbitProperties properties
    ) {
        this.events = events;
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public TripRecalculationEventRecord publish(Trip trip, RecalculationReason reason, String correlationId) {
        var record = events.save(new TripRecalculationEventRecord(trip, reason, correlationId));
        if (properties.isEnabled()) {
            publishAfterCommit(record);
        }
        return record;
    }

    private void publishAfterCommit(TripRecalculationEventRecord record) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publishToRabbit(record);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishToRabbit(record);
            }
        });
    }

    private void publishToRabbit(TripRecalculationEventRecord record) {
        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getTripRecalculationRequestedRoutingKey(),
            TripRecalculationRequestedEvent.from(record)
        );
    }
}
