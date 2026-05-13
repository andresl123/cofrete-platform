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
import java.time.LocalDate;

@Entity
@Table(name = "tax_rule_years")
class TaxRuleYear {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private TaxRegime regime;

    @Column(nullable = false)
    private int planningYear;

    @Column(precision = 14, scale = 2)
    private BigDecimal annualGrossLimit;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(columnDefinition = "text")
    private String formulaMetadata;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(length = 500)
    private String sourceUrl;

    private Instant reviewedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TaxRuleYear() {
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
}
