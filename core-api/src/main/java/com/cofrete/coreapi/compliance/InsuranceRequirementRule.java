package com.cofrete.coreapi.compliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "insurance_requirement_rules")
class InsuranceRequirementRule {

    @Id
    private String id;

    @Column(nullable = false, length = 120)
    private String requirementScope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private InsurancePolicyType policyType;

    @Column(nullable = false)
    private boolean required = true;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(length = 160)
    private String sourceName;

    @Column(length = 500)
    private String sourceUrl;

    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceRuleFreshnessStatus freshnessStatus = ComplianceRuleFreshnessStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RuleConfidence confidence = RuleConfidence.UNKNOWN;

    @Column(length = 700)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected InsuranceRequirementRule() {
    }

    InsuranceRequirementRule(InsuranceRequirementRuleRequest request) {
        id = "insurance_requirement_rule_" + UUID.randomUUID();
        updateFrom(request);
    }

    void updateFrom(InsuranceRequirementRuleRequest request) {
        requirementScope = request.requirementScope().trim();
        policyType = request.policyType();
        required = request.required() == null || request.required();
        effectiveFrom = request.effectiveFrom();
        effectiveTo = request.effectiveTo();
        sourceName = blankToNull(request.sourceName());
        sourceUrl = blankToNull(request.sourceUrl());
        reviewedAt = request.reviewedAt();
        freshnessStatus = request.freshnessStatus() == null
            ? ComplianceRuleFreshnessStatus.UNKNOWN
            : request.freshnessStatus();
        confidence = request.confidence() == null ? RuleConfidence.UNKNOWN : request.confidence();
        notes = blankToNull(request.notes());
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

    String getRequirementScope() {
        return requirementScope;
    }

    InsurancePolicyType getPolicyType() {
        return policyType;
    }

    boolean isRequired() {
        return required;
    }

    LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    String getSourceName() {
        return sourceName;
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

    String getNotes() {
        return notes;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
