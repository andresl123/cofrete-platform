package com.cofrete.financeworker.messaging;

import com.cofrete.financeworker.messaging.events.TripFinanceRecalculatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FinanceEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final FinanceWorkerRabbitProperties properties;

    public FinanceEventPublisher(RabbitTemplate rabbitTemplate, FinanceWorkerRabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishTripFinanceRecalculated(TripFinanceRecalculatedEvent event) {
        Assert.isTrue(
            FinanceEventNames.TRIP_FINANCE_RECALCULATED.equals(event.eventType()),
            () -> "Expected eventType " + FinanceEventNames.TRIP_FINANCE_RECALCULATED
                + " but received " + event.eventType()
        );

        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getTripFinanceRecalculatedRoutingKey(),
            event
        );
    }
}
