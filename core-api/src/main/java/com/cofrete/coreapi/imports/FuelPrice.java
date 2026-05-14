package com.cofrete.coreapi.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "fuel_prices")
class FuelPrice {

    @Id
    private String id;

    @Column(nullable = false, length = 40)
    private String fuelType;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(length = 120)
    private String city;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal pricePerLiter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 80)
    private String sourceType;

    @Column(length = 500)
    private String sourceUrl;

    @Column(nullable = false)
    private LocalDate sourcePeriodStart;

    @Column(nullable = false)
    private LocalDate sourcePeriodEnd;

    @Column(nullable = false)
    private Instant retrievedAt;

    @Column(nullable = false, length = 40)
    private String freshnessStatus;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(nullable = false, length = 64)
    private String importAuditId;

    @Column(nullable = false)
    private Instant createdAt;

    protected FuelPrice() {
    }

    String getId() {
        return id;
    }

    String getFuelType() {
        return fuelType;
    }

    String getState() {
        return state;
    }

    String getCity() {
        return city;
    }

    BigDecimal getPricePerLiter() {
        return pricePerLiter;
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

    String getSourceUrl() {
        return sourceUrl;
    }

    LocalDate getSourcePeriodStart() {
        return sourcePeriodStart;
    }

    LocalDate getSourcePeriodEnd() {
        return sourcePeriodEnd;
    }

    Instant getRetrievedAt() {
        return retrievedAt;
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
