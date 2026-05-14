package com.cofrete.financeworker.reserve;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRuleEvent;

class EventReserveAllocationInputSnapshotProvider implements ReserveAllocationInputSnapshotProvider {

    @Override
    public ReserveAllocationInputSnapshot load(ReserveAllocationRequestedEvent event) {
        return new ReserveAllocationInputSnapshot(
            event.allocationSubjectId(),
            event.allocationRevision(),
            event.accountId(),
            event.driverId(),
            event.grossAmount(),
            event.passThroughAmount(),
            event.distanceKm(),
            event.currency(),
            event.rules().stream().map(EventReserveAllocationInputSnapshotProvider::rule).toList()
        );
    }

    private static ReserveAllocationRule rule(ReserveAllocationRuleEvent event) {
        return new ReserveAllocationRule(
            ReserveBucket.valueOf(event.bucket()),
            ReserveRulePolicy.valueOf(event.policy()),
            event.rate(),
            event.fixedAmount(),
            event.perKmAmount()
        );
    }
}
