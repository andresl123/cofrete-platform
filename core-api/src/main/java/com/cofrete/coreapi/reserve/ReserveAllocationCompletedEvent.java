package com.cofrete.coreapi.reserve;

import java.time.Instant;
import java.util.Map;

record ReserveAllocationCompletedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String accountId,
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
    String allocationTraceId,
    String correlationId,
    String producer,
    Instant allocatedAt
) {

    static final String EVENT_TYPE = "reserve.allocation.completed";
    static final int VERSION = 1;
    static final String PRODUCER = "finance-worker";
}
