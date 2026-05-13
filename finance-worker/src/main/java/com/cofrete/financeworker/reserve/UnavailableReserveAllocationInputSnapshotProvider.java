package com.cofrete.financeworker.reserve;

import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;

class UnavailableReserveAllocationInputSnapshotProvider implements ReserveAllocationInputSnapshotProvider {

    @Override
    public ReserveAllocationInputSnapshot load(ReserveAllocationRequestedEvent event) {
        throw new UnsupportedOperationException(
            "Reserve allocation input snapshots are unavailable until Core API reserve read model integration is wired."
        );
    }
}
