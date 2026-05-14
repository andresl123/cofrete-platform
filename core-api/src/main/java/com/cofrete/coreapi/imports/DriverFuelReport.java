package com.cofrete.coreapi.imports;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "driver_fuel_reports")
class DriverFuelReport {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(nullable = false, length = 40)
    private String fuelType;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(length = 120)
    private String city;

    @Column(length = 160)
    private String stationName;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal pricePerLiter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 80)
    private String sourceType;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(length = 160)
    private String receiptReference;

    @Column(nullable = false)
    private Instant reportedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected DriverFuelReport() {
    }

    DriverFuelReport(String accountId, String driverId, DriverFuelReportRequest request) {
        id = DomainIds.prefixed("fuel_report");
        this.accountId = accountId;
        this.driverId = driverId;
        fuelType = normalize(request.fuelType());
        state = normalize(request.state());
        city = normalizeNullable(request.city());
        stationName = trimToNull(request.stationName());
        pricePerLiter = request.pricePerLiter();
        currency = request.currency().trim();
        source = "driver_report";
        sourceType = "driver_report";
        confidence = "driver_confirmed";
        receiptReference = trimToNull(request.receiptReference());
        reportedAt = request.reportedAt() == null ? Instant.now() : request.reportedAt();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : normalize(value);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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

    String getStationName() {
        return stationName;
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

    String getConfidence() {
        return confidence;
    }

    String getReceiptReference() {
        return receiptReference;
    }

    Instant getReportedAt() {
        return reportedAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
