package com.cofrete.financeworker.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.messaging.events.TripFinanceRecalculatedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class FinanceEventPublisherTests {

    private final CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
    private final FinanceWorkerRabbitProperties properties = new FinanceWorkerRabbitProperties();
    private final FinanceEventPublisher publisher = new FinanceEventPublisher(rabbitTemplate, properties);

    @Test
    void publishesTripFinanceRecalculatedWithCanonicalRoutingKey() {
        TripFinanceRecalculatedEvent event = recalculatedEvent(FinanceEventNames.TRIP_FINANCE_RECALCULATED);

        publisher.publishTripFinanceRecalculated(event);

        assertThat(rabbitTemplate.exchange).isEqualTo(properties.getExchange());
        assertThat(rabbitTemplate.routingKey).isEqualTo(FinanceEventNames.TRIP_FINANCE_RECALCULATED);
        assertThat(rabbitTemplate.message).isSameAs(event);
    }

    @Test
    void rejectsUnexpectedRecalculatedEventName() {
        TripFinanceRecalculatedEvent event = recalculatedEvent("trip.finance.updated");

        assertThatThrownBy(() -> publisher.publishTripFinanceRecalculated(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.TRIP_FINANCE_RECALCULATED);
    }

    private static TripFinanceRecalculatedEvent recalculatedEvent(String eventType) {
        return new TripFinanceRecalculatedEvent(
            eventType,
            1,
            "evt_124",
            "TRIP-001",
            "8000.00",
            "4900.00",
            "1600.00",
            "1100.00",
            "BRL",
            "GOOD",
            "corr_123",
            Instant.parse("2026-05-11T12:00:05Z")
        );
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        private String exchange;
        private String routingKey;
        private Object message;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }
    }
}
