package com.cofrete.coreapi.compliance;

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
import java.util.UUID;

@Entity
@Table(name = "waiting_time_rules")
class WaitingTimeRule {

    @Id
    private String id;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal thresholdHours;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal ratePerTonHour;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(length = 500)
    private String sourceUrl;

    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceRuleFreshnessStatus freshnessStatus = ComplianceRuleFreshnessStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RuleConfidence confidence = RuleConfidence.UNKNOWN;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WaitingTimeRule() {
    }

    WaitingTimeRule(WaitingTimeRuleRequest request) {
        id = "waiting_time_rule_" + UUID.randomUUID();
        updateFrom(request);
    }

    void updateFrom(WaitingTimeRuleRequest request) {
        thresholdHours = request.thresholdHours();
        ratePerTonHour = request.ratePerTonHour();
        currency = request.currency() == null ? "BRL" : request.currency();
        effectiveFrom = request.effectiveFrom();
        effectiveTo = request.effectiveTo();
        sourceUrl = request.sourceUrl() == null || request.sourceUrl().isBlank() ? null : request.sourceUrl().trim();
        reviewedAt = request.reviewedAt();
        freshnessStatus = request.freshnessStatus() == null
            ? ComplianceRuleFreshnessStatus.UNKNOWN
            : request.freshnessStatus();
        confidence = request.confidence() == null ? RuleConfidence.UNKNOWN : request.confidence();
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

    BigDecimal getThresholdHours() {
        return thresholdHours;
    }

    BigDecimal getRatePerTonHour() {
        return ratePerTonHour;
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

    String getSourceUrl() {
        return sourceUrl;
    }

    Instant getReviewedAt() {
        return reviewedAt;
    }

    ComplianceRuleFreshnessStatus getFreshnessStatus() {
        return freshnessStatus;
    }

    RuleConfidence getConfidence() {
        return confidence;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
