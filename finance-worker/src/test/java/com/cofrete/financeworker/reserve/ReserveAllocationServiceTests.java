package com.cofrete.financeworker.reserve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.messaging.FinanceEventNames;
import com.cofrete.financeworker.messaging.FinanceEventPublisher;
import com.cofrete.financeworker.messaging.FinanceWorkerRabbitProperties;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRuleEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class ReserveAllocationServiceTests {

    @Test
    void handlesReserveAllocationRequestedEventOncePerIdempotencyKey() {
        ReserveAllocationService service = new ReserveAllocationService(
            ignored -> snapshot(),
            new ReserveAllocationCalculator(),
            publisher(),
            new ProcessedReserveAllocationRegistry()
        );

        ReserveAllocationResult first = service.handle(event("reserve:pay_123:1"));
        ReserveAllocationResult duplicate = service.handle(event("reserve:pay_123:1"));

        assertThat(first.requiredReserveAmount()).isEqualTo("592.00");
        assertThat(first.safePersonalWithdrawal()).isEqualTo("6808.00");
        assertThat(duplicate).isNull();
    }

    @Test
    void rejectsMalformedReserveAllocationEvent() {
        ReserveAllocationService service = new ReserveAllocationService(
            ignored -> snapshot(),
            new ReserveAllocationCalculator(),
            publisher(),
            new ProcessedReserveAllocationRegistry()
        );

        assertThatThrownBy(() -> service.handle(new ReserveAllocationRequestedEvent(
            "reserve.allocate",
            1,
            "evt_125",
            "reserve:pay_123:1",
            "acct_123",
            "pay_123",
            1,
            "pay_123",
            "trip_123",
            "driver_123",
            "8000.00",
            "600.00",
            null,
            "BRL",
            "FREIGHT_PAYMENT_RECEIVED",
            List.of(ruleEvent()),
            "corr_123",
            "core-api",
            Instant.parse("2026-05-11T12:00:10Z")
        )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.RESERVE_ALLOCATION_REQUESTED);
    }

    @Test
    void retriesAfterSnapshotLoadFailureWithSameIdempotencyKey() {
        var registry = new ProcessedReserveAllocationRegistry();
        ReserveAllocationService failingService = new ReserveAllocationService(
            ignored -> {
                throw new IllegalStateException("snapshot unavailable");
            },
            new ReserveAllocationCalculator(),
            publisher(),
            registry
        );

        assertThatThrownBy(() -> failingService.handle(event("reserve:pay_123:retry")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("snapshot unavailable");

        ReserveAllocationService recoveredService = new ReserveAllocationService(
            ignored -> snapshot(),
            new ReserveAllocationCalculator(),
            publisher(),
            registry
        );

        assertThat(recoveredService.handle(event("reserve:pay_123:retry"))).isNotNull();
        assertThat(recoveredService.handle(event("reserve:pay_123:retry"))).isNull();
    }

    static ReserveAllocationRequestedEvent event(String idempotencyKey) {
        return new ReserveAllocationRequestedEvent(
            FinanceEventNames.RESERVE_ALLOCATION_REQUESTED,
            1,
            "evt_125",
            idempotencyKey,
            "acct_123",
            "pay_123",
            1,
            "pay_123",
            "trip_123",
            "driver_123",
            "8000.00",
            "600.00",
            null,
            "BRL",
            "FREIGHT_PAYMENT_RECEIVED",
            List.of(ruleEvent()),
            "corr_123",
            "core-api",
            Instant.parse("2026-05-11T12:00:10Z")
        );
    }

    private static ReserveAllocationInputSnapshot snapshot() {
        return new ReserveAllocationInputSnapshot(
            "pay_123",
            1,
            "acct_123",
            "driver_123",
            "8000.00",
            "600.00",
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

    private static ReserveAllocationRuleEvent ruleEvent() {
        return new ReserveAllocationRuleEvent(
            "MAINTENANCE",
            "PERCENT_OF_AMOUNT",
            "0.080000",
            null,
            null
        );
    }

    private static FinanceEventPublisher publisher() {
        return new FinanceEventPublisher(new CapturingRabbitTemplate(), new FinanceWorkerRabbitProperties());
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
        }
    }
}
