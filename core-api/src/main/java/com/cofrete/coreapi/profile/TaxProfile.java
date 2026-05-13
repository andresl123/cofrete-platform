package com.cofrete.coreapi.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tax_profiles")
class TaxProfile {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private TaxRegime regime;

    @Column(nullable = false)
    private int planningYear;

    @Column(precision = 14, scale = 2)
    private BigDecimal annualGrossLimit;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 160)
    private String source;

    @Column(length = 80)
    private String sourceType;

    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FreshnessStatus freshnessStatus;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TaxProfile() {
    }

    TaxProfile(Driver driver, TaxProfileRequest request) {
        id = DomainIds.prefixed("tax");
        this.driver = driver;
        updateFrom(request);
    }

    void updateFrom(TaxProfileRequest request) {
        regime = request.regime();
        planningYear = request.planningYear();
        annualGrossLimit = request.annualGrossLimit();
        currency = request.currency() == null ? "BRL" : request.currency().toUpperCase();
        source = blankToNull(request.source());
        sourceType = blankToNull(request.sourceType());
        reviewedAt = request.reviewedAt();
        freshnessStatus = request.freshnessStatus() == null ? FreshnessStatus.UNKNOWN : request.freshnessStatus();
        active = true;
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    String getId() {
        return id;
    }

    Driver getDriver() {
        return driver;
    }

    TaxRegime getRegime() {
        return regime;
    }

    int getPlanningYear() {
        return planningYear;
    }

    BigDecimal getAnnualGrossLimit() {
        return annualGrossLimit;
    }

    String getCurrency() {
        return currency;
    }

    String getSource() {
        return source;
    }

    String getSourceType() {
        return sourceType;
    }

    Instant getReviewedAt() {
        return reviewedAt;
    }

    FreshnessStatus getFreshnessStatus() {
        return freshnessStatus;
    }
}
