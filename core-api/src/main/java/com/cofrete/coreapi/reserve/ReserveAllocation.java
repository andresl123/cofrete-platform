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

    @Column(nullable = false)
    private Instant createdAt;

    protected ReserveAllocation() {
    }

    ReserveAllocation(
        String accountId,
        ReserveAllocationRequest request,
        String requestFingerprint,
        AllocationMathResult math,
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
        grossAmount = math.grossAmount();
        passThroughAmount = math.passThroughAmount();
        allocatableAmount = math.allocatableAmount();
        requiredReserveAmount = math.requiredReserveAmount();
        safePersonalWithdrawal = math.safePersonalWithdrawal();
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

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
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
}
