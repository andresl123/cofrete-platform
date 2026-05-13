package com.cofrete.financeworker.recalculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.calculation.ReservePolicy;
import com.cofrete.financeworker.calculation.TripFinanceCalculator;
import com.cofrete.financeworker.calculation.TripFinanceInputSnapshot;
import com.cofrete.financeworker.messaging.FinanceEventNames;
import com.cofrete.financeworker.messaging.FinanceEventPublisher;
import com.cofrete.financeworker.messaging.FinanceWorkerRabbitProperties;
import com.cofrete.financeworker.messaging.events.TripFinanceRecalculatedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class TripFinanceRecalculationServiceTests {

    private final CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
    private final FinanceWorkerRabbitProperties properties = new FinanceWorkerRabbitProperties();
    private final FinanceEventPublisher publisher = new FinanceEventPublisher(rabbitTemplate, properties);

    @Test
    void handlesCanonicalRequestAndPublishesRecalculatedEvent() {
        TripFinanceRecalculationService service = serviceReturning(snapshot());
        TripRecalculationRequestedEvent request = request(FinanceEventNames.TRIP_RECALCULATION_REQUESTED);

        boolean processed = service.handle(request);

        assertThat(processed).isTrue();
        assertThat(rabbitTemplate.publishCount).isEqualTo(1);
        assertThat(rabbitTemplate.exchange).isEqualTo(properties.getExchange());
        assertThat(rabbitTemplate.routingKey).isEqualTo(FinanceEventNames.TRIP_FINANCE_RECALCULATED);
        assertThat(rabbitTemplate.message).isInstanceOf(TripFinanceRecalculatedEvent.class);

        TripFinanceRecalculatedEvent event = (TripFinanceRecalculatedEvent) rabbitTemplate.message;
        assertThat(event.eventType()).isEqualTo(FinanceEventNames.TRIP_FINANCE_RECALCULATED);
        assertThat(event.version()).isEqualTo(1);
        assertThat(event.eventId()).startsWith("evt_");
        assertThat(event.idempotencyKey()).startsWith("trip:TRIP-216:finance-result:7:calc_");
        assertThat(event.tripId()).isEqualTo("TRIP-216");
        assertThat(event.driverId()).isEqualTo("DRIVER-001");
        assertThat(event.inputRevision()).isEqualTo(7);
        assertThat(event.grossFreight()).isEqualTo("8000.00");
        assertThat(event.passThroughAmount()).isEqualTo("600.00");
        assertThat(event.directTripCost()).isEqualTo("3000.00");
        assertThat(event.requiredReserves()).isEqualTo("1776.00");
        assertThat(event.safePersonalWithdrawal()).isEqualTo("1974.00");
        assertThat(event.expectedProfit()).isEqualTo("1974.00");
        assertThat(event.currency()).isEqualTo("BRL");
        assertThat(event.financialHealthImpact()).isEqualTo("GOOD");
        assertThat(event.sourceFreshness()).containsEntry("fuel", "CURRENT");
        assertThat(event.correlationId()).isEqualTo("corr_216");
        assertThat(event.producer()).isEqualTo("finance-worker");
        assertThat(event.calculatedAt()).isEqualTo(request.requestedAt());
    }

    @Test
    void suppressesDuplicateRequestsByIdempotencyKey() {
        TripFinanceRecalculationService service = serviceReturning(snapshot());
        TripRecalculationRequestedEvent request = request(FinanceEventNames.TRIP_RECALCULATION_REQUESTED);

        assertThat(service.handle(request)).isTrue();
        assertThat(service.handle(request)).isFalse();

        assertThat(rabbitTemplate.publishCount).isEqualTo(1);
    }

    @Test
    void rejectsUnexpectedRequestEventName() {
        TripFinanceRecalculationService service = serviceReturning(snapshot());

        assertThatThrownBy(() -> service.handle(request("trip.finance.recalculate.requested")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.TRIP_RECALCULATION_REQUESTED);
    }

    private TripFinanceRecalculationService serviceReturning(TripFinanceInputSnapshot snapshot) {
        return new TripFinanceRecalculationService(
            ignored -> snapshot,
            new TripFinanceCalculator(),
            publisher,
            new ProcessedRecalculationRegistry()
        );
    }

    private static TripRecalculationRequestedEvent request(String eventType) {
        return new TripRecalculationRequestedEvent(
            eventType,
            1,
            "evt_216",
            "trip:TRIP-216:recalculation:7",
            "TRIP-216",
            "DRIVER-001",
            "TRUCK-001",
            7,
            "EXPENSE_CREATED",
            "corr_216",
            "core-api",
            Instant.parse("2026-05-11T12:00:00Z")
        );
    }

    private static TripFinanceInputSnapshot snapshot() {
        return new TripFinanceInputSnapshot(
            "TRIP-216",
            "DRIVER-001",
            "TRUCK-001",
            7,
            "BRL",
            "8000.00",
            "1000.00",
            "2.50",
            "5.25",
            "120.00",
            "300.00",
            "385.70",
            "214.30",
            "0.00",
            "280.00",
            "200.00",
            "650.00",
            new ReservePolicy("0.0800", "0.0400", "0.0300", "0.0200", "0.0500", "0.0200"),
            Map.of(
                "fuel", "CURRENT",
                "toll", "CURRENT",
                "tax", "CURRENT",
                "compliance", "UNKNOWN"
            )
        );
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        private String exchange;
        private String routingKey;
        private Object message;
        private int publishCount;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
            this.publishCount++;
        }
    }
}
