package com.cofrete.coreapi.profile;

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
import java.util.UUID;

@Entity
@Table(name = "ipva_rules")
class IpvaRule {

    @Id
    private String id;

    @Column(nullable = false, length = 2)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private int effectiveYear;

    @Column(precision = 7, scale = 4)
    private BigDecimal ratePercent;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String sourceUrl;

    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FreshnessStatus freshnessStatus = FreshnessStatus.UNKNOWN;

    @Column(length = 500)
    private String licensingSourceUrl;

    private Instant licensingReviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FreshnessStatus licensingFreshnessStatus = FreshnessStatus.UNKNOWN;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected IpvaRule() {
    }

    IpvaRule(IpvaRuleRequest request) {
        id = "ipva_rule_" + UUID.randomUUID();
        updateFrom(request);
    }

    void updateFrom(IpvaRuleRequest request) {
        state = request.state().trim().toUpperCase();
        vehicleType = request.vehicleType();
        effectiveYear = request.effectiveYear();
        ratePercent = request.ratePercent();
        currency = request.currency() == null ? "BRL" : request.currency();
        sourceUrl = blankToNull(request.sourceUrl());
        reviewedAt = request.reviewedAt();
        freshnessStatus = request.freshnessStatus() == null ? FreshnessStatus.UNKNOWN : request.freshnessStatus();
        licensingSourceUrl = blankToNull(request.licensingSourceUrl());
        licensingReviewedAt = request.licensingReviewedAt();
        licensingFreshnessStatus = request.licensingFreshnessStatus() == null
            ? FreshnessStatus.UNKNOWN
            : request.licensingFreshnessStatus();
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

    String getState() {
        return state;
    }

    VehicleType getVehicleType() {
        return vehicleType;
    }

    int getEffectiveYear() {
        return effectiveYear;
    }

    BigDecimal getRatePercent() {
        return ratePercent;
    }

    String getCurrency() {
        return currency;
    }

    String getSourceUrl() {
        return sourceUrl;
    }

    Instant getReviewedAt() {
        return reviewedAt;
    }

    FreshnessStatus getFreshnessStatus() {
        return freshnessStatus;
    }

    String getLicensingSourceUrl() {
        return licensingSourceUrl;
    }

    Instant getLicensingReviewedAt() {
        return licensingReviewedAt;
    }

    FreshnessStatus getLicensingFreshnessStatus() {
        return licensingFreshnessStatus;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
