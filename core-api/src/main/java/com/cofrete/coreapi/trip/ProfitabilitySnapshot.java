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
@Table(name = "profitability_snapshots")
class ProfitabilitySnapshot {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private int inputRevision;

    @Column(nullable = false, length = 80)
    private String calculationTraceId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossFreight;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal passThroughAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal directTripCost;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal requiredReserves;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal safePersonalWithdrawal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedProfit;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal marginPercent;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FinancialHealthStatus financialHealthStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProfitabilityRecommendation recommendation;

    @Column(nullable = false, length = 500)
    private String sourceFreshness;

    @Column(nullable = false)
    private Instant createdAt;

    protected ProfitabilitySnapshot() {
    }

    ProfitabilitySnapshot(Trip trip, TripCostInput input, ProfitabilityMathResult result) {
        id = DomainIds.prefixed("profit_snapshot");
        this.trip = trip;
        inputRevision = input.getInputRevision();
        calculationTraceId = result.calculationTraceId();
        grossFreight = result.grossFreight();
        passThroughAmount = result.passThroughAmount();
        directTripCost = result.directTripCost();
        requiredReserves = result.requiredReserves();
        safePersonalWithdrawal = result.safePersonalWithdrawal();
        expectedProfit = result.expectedProfit();
        marginPercent = result.marginPercent();
        currency = trip.getFreight().getCurrency();
        financialHealthStatus = result.financialHealthStatus();
        recommendation = result.recommendation();
        sourceFreshness = input.getSourceFreshness();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    String getId() {
        return id;
    }

    String getTripId() {
        return trip.getId();
    }

    int getInputRevision() {
        return inputRevision;
    }

    String getCalculationTraceId() {
        return calculationTraceId;
    }

    BigDecimal getGrossFreight() {
        return grossFreight;
    }

    BigDecimal getPassThroughAmount() {
        return passThroughAmount;
    }

    BigDecimal getDirectTripCost() {
        return directTripCost;
    }

    BigDecimal getRequiredReserves() {
        return requiredReserves;
    }

    BigDecimal getSafePersonalWithdrawal() {
        return safePersonalWithdrawal;
    }

    BigDecimal getExpectedProfit() {
        return expectedProfit;
    }

    BigDecimal getMarginPercent() {
        return marginPercent;
    }

    String getCurrency() {
        return currency;
    }

    FinancialHealthStatus getFinancialHealthStatus() {
        return financialHealthStatus;
    }

    ProfitabilityRecommendation getRecommendation() {
        return recommendation;
    }

    String getSourceFreshness() {
        return sourceFreshness;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
