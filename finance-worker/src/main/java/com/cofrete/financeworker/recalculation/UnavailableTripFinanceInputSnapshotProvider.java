package com.cofrete.financeworker.recalculation;

import com.cofrete.financeworker.calculation.TripFinanceInputSnapshot;

final class UnavailableTripFinanceInputSnapshotProvider implements TripFinanceInputSnapshotProvider {

    @Override
    public TripFinanceInputSnapshot loadSnapshot(TripFinanceInputRequest request) {
        throw new IllegalStateException(
            "No TripFinanceInputSnapshotProvider is configured for trip "
                + request.tripId() + " revision " + request.inputRevision()
        );
    }
}
