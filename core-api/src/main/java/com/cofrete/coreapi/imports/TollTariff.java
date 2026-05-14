package com.cofrete.coreapi.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "toll_tariffs")
class TollTariff {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "toll_plaza_id", nullable = false)
    private TollPlaza plaza;

    @Column(nullable = false, length = 40)
    private String vehicleCategory;

    @Column(nullable = false)
    private int axleCount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate effectiveStart;

    private LocalDate effectiveEnd;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 40)
    private String freshnessStatus;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(nullable = false, length = 64)
    private String importAuditId;

    @Column(nullable = false)
    private Instant createdAt;

    protected TollTariff() {
    }

    TollPlaza getPlaza() {
        return plaza;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    LocalDate getEffectiveStart() {
        return effectiveStart;
    }

    String getFreshnessStatus() {
        return freshnessStatus;
    }

    String getConfidence() {
        return confidence;
    }

    String getImportAuditId() {
        return importAuditId;
    }
}
