package com.cofrete.financeworker.messaging.events;

import java.time.Instant;
import java.util.Map;

public record ReserveAllocationCompletedEvent(
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
    Map<String, String> bucketAllocations,
    String allocationTraceId,
    String correlationId,
    String producer,
    Instant allocatedAt
) {
}
