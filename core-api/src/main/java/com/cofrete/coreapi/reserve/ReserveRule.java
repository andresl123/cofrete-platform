package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reserve_rules")
class ReserveRule {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ReserveBucket bucket;

    @Column(length = 60)
    private String activeBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReserveRulePolicy policy;

    @Column(precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(precision = 14, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 14, scale = 4)
    private BigDecimal perKmAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal targetBalance;

    @Column(nullable = false, length = 3)
    private String currency = "BRL";

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 240)
    private String sourceAssumption;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ReserveRule() {
    }

    ReserveRule(String accountId, ReserveRuleRequest request) {
        id = DomainIds.prefixed("reserve_rule");
        this.accountId = accountId;
        updateFrom(request);
    }

    void updateFrom(ReserveRuleRequest request) {
        bucket = request.bucket();
        policy = request.policy();
        rate = request.rate();
        fixedAmount = request.fixedAmount();
        perKmAmount = request.perKmAmount();
        targetBalance = request.targetBalance();
        currency = request.currency() == null ? "BRL" : request.currency();
        effectiveFrom = request.effectiveFrom();
        effectiveTo = request.effectiveTo();
        active = request.active() == null || request.active();
        activeBucket = active ? bucket.name() : null;
        sourceAssumption = request.sourceAssumption();
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    ReserveBucket getBucket() {
        return bucket;
    }

    ReserveRulePolicy getPolicy() {
        return policy;
    }

    BigDecimal getRate() {
        return rate;
    }

    BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    BigDecimal getPerKmAmount() {
        return perKmAmount;
    }

    BigDecimal getTargetBalance() {
        return targetBalance;
    }

    String getCurrency() {
        return currency;
    }

    LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    boolean isActive() {
        return active;
    }

    String getSourceAssumption() {
        return sourceAssumption;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
