package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trip_cost_inputs")
class TripCostInput {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private int inputRevision;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal dieselConsumptionKmPerLiter;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal dieselPricePerLiter;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal arlaCost;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal nonReimbursedToll;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tollReimbursement;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valePedagio;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal otherPassThrough;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal mealsAndLodgingCost;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal otherDirectCost;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal financingAllocation;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal maintenanceRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal tireRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal insuranceRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal replacementRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal emergencyRate;

    @Column(nullable = false, length = 500)
    private String sourceFreshness;

    @Column(nullable = false)
    private Instant createdAt;

    protected TripCostInput() {
    }

    TripCostInput(Trip trip, int inputRevision, ProfitabilityEstimateRequest request) {
        id = DomainIds.prefixed("trip_cost_input");
        this.trip = trip;
        this.inputRevision = inputRevision;
        dieselConsumptionKmPerLiter = request.dieselConsumptionKmPerLiter();
        dieselPricePerLiter = request.dieselPricePerLiter();
        arlaCost = TripMoney.optionalMoney(request.arlaCost());
        nonReimbursedToll = TripMoney.optionalMoney(request.nonReimbursedToll());
        tollReimbursement = TripMoney.optionalMoney(request.tollReimbursement());
        valePedagio = TripMoney.optionalMoney(request.valePedagio());
        otherPassThrough = TripMoney.optionalMoney(request.otherPassThrough());
        mealsAndLodgingCost = TripMoney.optionalMoney(request.mealsAndLodgingCost());
        otherDirectCost = TripMoney.optionalMoney(request.otherDirectCost());
        financingAllocation = TripMoney.optionalMoney(request.financingAllocation());
        maintenanceRate = TripMoney.rate(request.reservePolicy().maintenanceRate(), "maintenanceRate");
        tireRate = TripMoney.rate(request.reservePolicy().tireRate(), "tireRate");
        taxRate = TripMoney.rate(request.reservePolicy().taxRate(), "taxRate");
        insuranceRate = TripMoney.rate(request.reservePolicy().insuranceRate(), "insuranceRate");
        replacementRate = TripMoney.rate(request.reservePolicy().replacementRate(), "replacementRate");
        emergencyRate = TripMoney.rate(request.reservePolicy().emergencyRate(), "emergencyRate");
        sourceFreshness = request.normalizedSourceFreshness();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    int getInputRevision() {
        return inputRevision;
    }

    BigDecimal getDieselConsumptionKmPerLiter() {
        return dieselConsumptionKmPerLiter;
    }

    BigDecimal getDieselPricePerLiter() {
        return dieselPricePerLiter;
    }

    BigDecimal getArlaCost() {
        return arlaCost;
    }

    BigDecimal getNonReimbursedToll() {
        return nonReimbursedToll;
    }

    BigDecimal getTollReimbursement() {
        return tollReimbursement;
    }

    BigDecimal getValePedagio() {
        return valePedagio;
    }

    BigDecimal getOtherPassThrough() {
        return otherPassThrough;
    }

    BigDecimal getMealsAndLodgingCost() {
        return mealsAndLodgingCost;
    }

    BigDecimal getOtherDirectCost() {
        return otherDirectCost;
    }

    BigDecimal getFinancingAllocation() {
        return financingAllocation;
    }

    BigDecimal getMaintenanceRate() {
        return maintenanceRate;
    }

    BigDecimal getTireRate() {
        return tireRate;
    }

    BigDecimal getTaxRate() {
        return taxRate;
    }

    BigDecimal getInsuranceRate() {
        return insuranceRate;
    }

    BigDecimal getReplacementRate() {
        return replacementRate;
    }

    BigDecimal getEmergencyRate() {
        return emergencyRate;
    }

    String getSourceFreshness() {
        return sourceFreshness;
    }
}
