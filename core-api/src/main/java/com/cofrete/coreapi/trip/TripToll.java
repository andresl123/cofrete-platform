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
@Table(name = "trip_tolls")
class TripToll {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(length = 160)
    private String plazaName;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TollPaidBy paidBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TollClassification classification;

    @Column(nullable = false, length = 80)
    private String financeTreatment;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(length = 120)
    private String source;

    @Column(length = 160)
    private String sourceReference;

    private Instant paidAt;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected TripToll() {
    }

    TripToll(Trip trip, ManualTollPaymentRequest request) {
        id = DomainIds.prefixed("trip_toll");
        this.trip = trip;
        plazaName = trimToNull(request.plazaName());
        amount = TripMoney.money(request.amount(), "amount");
        currency = request.currency().trim();
        paidBy = request.paidBy() == null ? TollPaidBy.UNKNOWN : request.paidBy();
        classification = request.classification();
        financeTreatment = financeTreatment(classification);
        confidence = defaultString(request.confidence(), "driver_reported");
        source = defaultString(request.source(), "driver_manual_entry");
        sourceReference = trimToNull(request.sourceReference());
        paidAt = request.paidAt();
        note = trimToNull(request.note());
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    static String financeTreatment(TollClassification classification) {
        return switch (classification) {
            case PASS_THROUGH -> "pass_through_or_reimbursement_not_profit";
            case DRIVER_PAID_NON_REIMBURSED -> "driver_cost_reduces_profit";
            case INCLUDED_IN_FREIGHT -> "warning_may_distort_profit";
            case NO_TOLL -> "no_toll_cost";
            case UNKNOWN -> "manual_review_needed";
        };
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

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    String getPlazaName() {
        return plazaName;
    }

    TollPaidBy getPaidBy() {
        return paidBy;
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

    String getSource() {
        return source;
    }

    String getSourceReference() {
        return sourceReference;
    }

    Instant getPaidAt() {
        return paidAt;
    }

    String getNote() {
        return note;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
