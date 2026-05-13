package com.cofrete.financeworker.reserve;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record ReserveAllocationResult(
    String allocationSubjectId,
    int allocationRevision,
    String driverId,
    String grossAmount,
    String passThroughAmount,
    String allocatableAmount,
    String requiredReserveAmount,
    String safePersonalWithdrawal,
    String currency,
    Map<ReserveBucket, String> bucketAllocations,
    String allocationTraceId
) {

    public ReserveAllocationResult {
        bucketAllocations = Collections.unmodifiableMap(new TreeMap<>(bucketAllocations));
    }
}
