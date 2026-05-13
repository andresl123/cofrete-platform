package com.cofrete.financeworker.reserve;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;

@FunctionalInterface
public interface ReserveAllocationInputSnapshotProvider {

    ReserveAllocationInputSnapshot load(ReserveAllocationRequestedEvent event);
}
