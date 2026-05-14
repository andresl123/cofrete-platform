package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.profile.DomainIds;
import java.time.Instant;
import java.util.List;

record ReserveAllocationRequestedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String accountId,
    String allocationSubjectId,
    int allocationRevision,
    String freightPaymentId,
    String tripId,
    String driverId,
    String grossAmount,
    String passThroughAmount,
    String distanceKm,
    String currency,
    String reason,
    List<ReserveAllocationRuleEvent> rules,
    String correlationId,
    String producer,
    Instant requestedAt
) {

    static final String EVENT_TYPE = "reserve.allocation.requested";
    static final int VERSION = 1;
    static final String PRODUCER = "core-api";

    static ReserveAllocationRequestedEvent from(
        String accountId,
        String driverId,
        ReserveAllocationRequest request,
        List<ReserveRule> activeRules
    ) {
        return new ReserveAllocationRequestedEvent(
            EVENT_TYPE,
            VERSION,
            DomainIds.prefixed("evt"),
            request.idempotencyKey(),
            accountId,
            request.allocationSubjectId(),
            request.allocationRevision(),
            request.freightPaymentId(),
            request.tripId(),
            driverId,
            ReserveMoney.money(request.grossAmount(), "grossAmount").toPlainString(),
            ReserveMoney.money(request.passThroughAmount(), "passThroughAmount").toPlainString(),
            request.distanceKm() == null ? null : ReserveMoney.distance(request.distanceKm()).toPlainString(),
            request.currency(),
            request.reason().name(),
            activeRules.stream().map(ReserveAllocationRuleEvent::from).toList(),
            "reserve-allocation:" + request.idempotencyKey(),
            PRODUCER,
            request.requestedAt() == null ? Instant.now() : request.requestedAt()
        );
    }
}

record ReserveAllocationRuleEvent(
    ReserveBucket bucket,
    ReserveRulePolicy policy,
    String rate,
    String fixedAmount,
    String perKmAmount
) {

    static ReserveAllocationRuleEvent from(ReserveRule rule) {
        return new ReserveAllocationRuleEvent(
            rule.getBucket(),
            rule.getPolicy(),
            ReserveMoney.optional(rule.getRate()),
            ReserveMoney.optionalMoney(rule.getFixedAmount()),
            ReserveMoney.optionalPerKm(rule.getPerKmAmount())
        );
    }
}
