package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reserve_allocations")
class ReserveAllocation {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, length = 160)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String requestFingerprint;

    @Column(length = 64)
    private String tripId;

    @Column(length = 64)
    private String freightPaymentId;

    @Column(length = 64)
    private String allocationSubjectId;

    @Column(nullable = false)
    private int allocationRevision;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal passThroughAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal allocatableAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal requiredReserveAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal safePersonalWithdrawal;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ReserveAllocationReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReserveAllocationStatus status;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(length = 160)
    private String resultEventId;

    @Column(length = 80)
    private String allocationTraceId;

    @Column
    private Instant allocatedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReserveAllocation() {
    }

    ReserveAllocation(
        String accountId,
        ReserveAllocationRequest request,
        String requestFingerprint,
        BigDecimal allocatableAmount,
        ReserveAllocationStatus status
    ) {
        id = DomainIds.prefixed("reserve_alloc");
        this.accountId = accountId;
        idempotencyKey = request.idempotencyKey();
        this.requestFingerprint = requestFingerprint;
        tripId = request.tripId();
        freightPaymentId = request.freightPaymentId();
        allocationSubjectId = request.allocationSubjectId();
        allocationRevision = request.allocationRevision();
        grossAmount = ReserveMoney.money(request.grossAmount(), "grossAmount");
        passThroughAmount = ReserveMoney.money(request.passThroughAmount(), "passThroughAmount");
        this.allocatableAmount = allocatableAmount;
        requiredReserveAmount = BigDecimal.ZERO.setScale(2);
        safePersonalWithdrawal = BigDecimal.ZERO.setScale(2);
        currency = request.currency();
        reason = request.reason();
        this.status = status;
        requestedAt = request.requestedAt() == null ? Instant.now() : request.requestedAt();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    boolean hasSameFingerprint(String fingerprint) {
        return requestFingerprint.equals(fingerprint);
    }

    boolean isRequested() {
        return status == ReserveAllocationStatus.REQUESTED;
    }

    void applyWorkerResult(ReserveAllocationCompletedEvent event) {
        grossAmount = resultMoney(event.grossAmount(), "grossAmount");
        passThroughAmount = resultMoney(event.passThroughAmount(), "passThroughAmount");
        allocatableAmount = resultMoney(event.allocatableAmount(), "allocatableAmount");
        requiredReserveAmount = resultMoney(event.requiredReserveAmount(), "requiredReserveAmount");
        safePersonalWithdrawal = resultMoney(event.safePersonalWithdrawal(), "safePersonalWithdrawal");
        currency = event.currency();
        resultEventId = event.eventId();
        allocationTraceId = event.allocationTraceId();
        allocatedAt = event.allocatedAt();
        status = ReserveAllocationStatus.ALLOCATED;
    }

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    String getTripId() {
        return tripId;
    }

    String getFreightPaymentId() {
        return freightPaymentId;
    }

    String getAllocationSubjectId() {
        return allocationSubjectId;
    }

    int getAllocationRevision() {
        return allocationRevision;
    }

    BigDecimal getGrossAmount() {
        return grossAmount;
    }

    BigDecimal getPassThroughAmount() {
        return passThroughAmount;
    }

    BigDecimal getAllocatableAmount() {
        return allocatableAmount;
    }

    BigDecimal getRequiredReserveAmount() {
        return requiredReserveAmount;
    }

    BigDecimal getSafePersonalWithdrawal() {
        return safePersonalWithdrawal;
    }

    String getCurrency() {
        return currency;
    }

    ReserveAllocationReason getReason() {
        return reason;
    }

    ReserveAllocationStatus getStatus() {
        return status;
    }

    Instant getRequestedAt() {
        return requestedAt;
    }

    String getAllocationTraceId() {
        return allocationTraceId;
    }

    Instant getAllocatedAt() {
        return allocatedAt;
    }

    private static BigDecimal resultMoney(String value, String fieldName) {
        return ReserveMoney.money(new BigDecimal(value), fieldName);
    }
}
