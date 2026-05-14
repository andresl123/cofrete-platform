package com.cofrete.financeworker.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.calculation.ReservePolicy;
import com.cofrete.financeworker.calculation.TripFinanceCalculator;
import com.cofrete.financeworker.calculation.TripFinanceInputSnapshot;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRuleEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import com.cofrete.financeworker.recalculation.ProcessedRecalculationRegistry;
import com.cofrete.financeworker.recalculation.TripFinanceRecalculationService;
import com.cofrete.financeworker.reserve.ProcessedReserveAllocationRegistry;
import com.cofrete.financeworker.reserve.ReserveAllocationCalculator;
import com.cofrete.financeworker.reserve.ReserveAllocationInputSnapshot;
import com.cofrete.financeworker.reserve.ReserveAllocationRule;
import com.cofrete.financeworker.reserve.ReserveAllocationService;
import com.cofrete.financeworker.reserve.ReserveBucket;
import com.cofrete.financeworker.reserve.ReserveRulePolicy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class FinanceEventListenersTests {

    private final FinanceEventListeners listeners = new FinanceEventListeners(
        recalculationService(),
        reserveAllocationService()
    );

    @Test
    void acceptsTripRecalculationRequestedEventAndCalculates() {
        listeners.handleTripRecalculationRequested(new TripRecalculationRequestedEvent(
            FinanceEventNames.TRIP_RECALCULATION_REQUESTED,
            1,
            "evt_123",
            "trip:TRIP-001:recalculation:1",
            "TRIP-001",
            "DRIVER-001",
            "TRUCK-001",
            1,
            "EXPENSE_CREATED",
            "corr_123",
            "core-api",
            Instant.parse("2026-05-11T12:00:00Z")
        ));
    }

    @Test
    void acceptsReserveAllocationRequestedEventAndAllocates() {
        listeners.handleReserveAllocationRequested(new ReserveAllocationRequestedEvent(
            FinanceEventNames.RESERVE_ALLOCATION_REQUESTED,
            1,
            "evt_125",
            "reserve:PAY-001:1",
            "ACCT-001",
            "PAY-001",
            1,
            "PAY-001",
            "TRIP-001",
            "DRIVER-001",
            "8000.00",
            "0.00",
            null,
            "BRL",
            "FREIGHT_PAYMENT_RECEIVED",
            List.of(new ReserveAllocationRuleEvent(
                "MAINTENANCE",
                "PERCENT_OF_AMOUNT",
                "0.080000",
                null,
                null
            )),
            "corr_123",
            "core-api",
            Instant.parse("2026-05-11T12:00:10Z")
        ));
    }

    @Test
    void rejectsLegacyTripRecalculationEventName() {
        TripRecalculationRequestedEvent event = new TripRecalculationRequestedEvent(
            "trip.finance.recalculate.requested",
            1,
            "evt_legacy",
            "trip:TRIP-001:recalculation:1",
            "TRIP-001",
            "DRIVER-001",
            "TRUCK-001",
            1,
            "EXPENSE_CREATED",
            "corr_123",
            "core-api",
            Instant.parse("2026-05-11T12:00:00Z")
        );

        assertThatThrownBy(() -> listeners.handleTripRecalculationRequested(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.TRIP_RECALCULATION_REQUESTED);
    }

    private static TripFinanceRecalculationService recalculationService() {
        FinanceWorkerRabbitProperties properties = new FinanceWorkerRabbitProperties();
        FinanceEventPublisher publisher = new FinanceEventPublisher(new CapturingRabbitTemplate(), properties);
        return new TripFinanceRecalculationService(
            ignored -> snapshot(),
            new TripFinanceCalculator(),
            publisher,
            new ProcessedRecalculationRegistry()
        );
    }

    private static ReserveAllocationService reserveAllocationService() {
        return new ReserveAllocationService(
            ignored -> reserveSnapshot(),
            new ReserveAllocationCalculator(),
            new FinanceEventPublisher(new CapturingRabbitTemplate(), new FinanceWorkerRabbitProperties()),
            new ProcessedReserveAllocationRegistry()
        );
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
        }
    }

    private static TripFinanceInputSnapshot snapshot() {
        return new TripFinanceInputSnapshot(
            "TRIP-001",
            "DRIVER-001",
            "TRUCK-001",
            1,
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

    private static ReserveAllocationInputSnapshot reserveSnapshot() {
        return new ReserveAllocationInputSnapshot(
            "PAY-001",
            1,
            "ACCT-001",
            "DRIVER-001",
            "8000.00",
            "0.00",
            null,
            "BRL",
            List.of(new ReserveAllocationRule(
                ReserveBucket.MAINTENANCE,
                ReserveRulePolicy.PERCENT_OF_AMOUNT,
                "0.080000",
                null,
                null
            ))
        );
    }
}
