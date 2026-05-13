package com.cofrete.financeworker.recalculation;

import com.cofrete.financeworker.calculation.TripFinanceInputSnapshot;

public interface TripFinanceInputSnapshotProvider {

    TripFinanceInputSnapshot loadSnapshot(TripFinanceInputRequest request);
}
