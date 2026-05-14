package com.cofrete.financeworker.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.messaging.events.ReserveAllocationCompletedEvent;
import com.cofrete.financeworker.messaging.events.TripFinanceRecalculatedEvent;
import java.time.Instant;
import java.util.Map;
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

    @Test
    void publishesReserveAllocationCompletedWithCanonicalRoutingKey() {
        ReserveAllocationCompletedEvent event = reserveCompletedEvent(FinanceEventNames.RESERVE_ALLOCATION_COMPLETED);

        publisher.publishReserveAllocationCompleted(event);

        assertThat(rabbitTemplate.exchange).isEqualTo(properties.getExchange());
        assertThat(rabbitTemplate.routingKey).isEqualTo(FinanceEventNames.RESERVE_ALLOCATION_COMPLETED);
        assertThat(rabbitTemplate.message).isSameAs(event);
    }

    @Test
    void rejectsUnexpectedReserveAllocationCompletedEventName() {
        ReserveAllocationCompletedEvent event = reserveCompletedEvent("reserve.allocation.updated");

        assertThatThrownBy(() -> publisher.publishReserveAllocationCompleted(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.RESERVE_ALLOCATION_COMPLETED);
    }

    private static TripFinanceRecalculatedEvent recalculatedEvent(String eventType) {
        return new TripFinanceRecalculatedEvent(
            eventType,
            1,
            "evt_124",
            "trip:TRIP-001:finance-result:1:calc_123",
            "TRIP-001",
            "DRIVER-001",
            1,
            "calc_123",
            "8000.00",
            "385.70",
            "4900.00",
            "1600.00",
            "1100.00",
            "1100.00",
            "BRL",
            "GOOD",
            Map.of(
                "fuel", "CURRENT",
                "toll", "CURRENT",
                "tax", "CURRENT",
                "compliance", "UNKNOWN"
            ),
            "corr_123",
            "finance-worker",
            Instant.parse("2026-05-11T12:00:05Z")
        );
    }

    private static ReserveAllocationCompletedEvent reserveCompletedEvent(String eventType) {
        return new ReserveAllocationCompletedEvent(
            eventType,
            1,
            "evt_125",
            "reserve:pay_123:1",
            "acct_123",
            "pay_123",
            1,
            "driver_123",
            "8000.00",
            "600.00",
            "7400.00",
            "592.00",
            "6808.00",
            "BRL",
            Map.of("MAINTENANCE", "592.00"),
            "reserve_alloc_123",
            "corr_123",
            "finance-worker",
            Instant.parse("2026-05-11T12:00:10Z")
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
