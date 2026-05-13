package com.cofrete.financeworker.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FinanceEventListenersTests {

    private final FinanceEventListeners listeners = new FinanceEventListeners();

    @Test
    void acceptsTripRecalculationRequestedEventStubWithoutCalculating() {
        listeners.handleTripRecalculationRequested(new TripRecalculationRequestedEvent(
            FinanceEventNames.TRIP_RECALCULATION_REQUESTED,
            1,
            "evt_123",
            "TRIP-001",
            "DRIVER-001",
            "TRUCK-001",
            "EXPENSE_CREATED",
            "corr_123",
            Instant.parse("2026-05-11T12:00:00Z")
        ));
    }

    @Test
    void acceptsReserveAllocationRequestedEventStubWithoutAllocating() {
        listeners.handleReserveAllocationRequested(new ReserveAllocationRequestedEvent(
            FinanceEventNames.RESERVE_ALLOCATION_REQUESTED,
            1,
            "evt_125",
            "PAY-001",
            "TRIP-001",
            "8000.00",
            "BRL",
            "corr_123",
            Instant.parse("2026-05-11T12:00:10Z")
        ));
    }

    @Test
    void rejectsLegacyTripRecalculationEventName() {
        TripRecalculationRequestedEvent event = new TripRecalculationRequestedEvent(
            "trip.finance.recalculate.requested",
            1,
            "evt_legacy",
            "TRIP-001",
            "DRIVER-001",
            "TRUCK-001",
            "EXPENSE_CREATED",
            "corr_123",
            Instant.parse("2026-05-11T12:00:00Z")
        );

        assertThatThrownBy(() -> listeners.handleTripRecalculationRequested(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(FinanceEventNames.TRIP_RECALCULATION_REQUESTED);
    }
}
