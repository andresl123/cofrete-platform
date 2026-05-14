package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vale_pedagio_records")
class ValePedagioRecord {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(length = 160)
    private String provider;

    @Column(length = 160)
    private String proofReference;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ValePedagioStatus receivedStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TollClassification classification;

    @Column(nullable = false, length = 80)
    private String financeTreatment;

    @Column(nullable = false, length = 80)
    private String confidence;

    private Instant receivedAt;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected ValePedagioRecord() {
    }

    ValePedagioRecord(Trip trip, ValePedagioRequest request) {
        id = DomainIds.prefixed("vale_pedagio");
        this.trip = trip;
        provider = trimToNull(request.provider());
        proofReference = trimToNull(request.proofReference());
        amount = TripMoney.money(request.amount(), "amount");
        currency = request.currency().trim();
        receivedStatus = request.receivedStatus();
        classification = receivedStatus == ValePedagioStatus.VALE_PEDAGIO_RECEIVED
            ? TollClassification.PASS_THROUGH
            : TollClassification.UNKNOWN;
        financeTreatment = receivedStatus == ValePedagioStatus.VALE_PEDAGIO_RECEIVED
            ? "vale_pedagio_pass_through_not_profit"
            : "pre_trip_alert_manual_review_needed";
        confidence = defaultString(request.confidence(), "driver_reported");
        receivedAt = request.receivedAt();
        note = trimToNull(request.note());
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    private static String defaultString(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    String getId() {
        return id;
    }

    String getProvider() {
        return provider;
    }

    String getProofReference() {
        return proofReference;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    ValePedagioStatus getReceivedStatus() {
        return receivedStatus;
    }

    TollClassification getClassification() {
        return classification;
    }

    String getFinanceTreatment() {
        return financeTreatment;
    }

    String getConfidence() {
        return confidence;
    }

    Instant getReceivedAt() {
        return receivedAt;
    }

    String getNote() {
        return note;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
