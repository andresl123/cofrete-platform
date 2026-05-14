package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "freights")
class Freight {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossFreight;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(precision = 14, scale = 2)
    private BigDecimal advanceAmount;

    private Integer balanceDueDays;

    @Column(length = 240)
    private String cargoDescription;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Freight() {
    }

    Freight(String accountId, String driverId, CreateTripRequest request) {
        id = DomainIds.prefixed("freight");
        this.accountId = accountId;
        this.driverId = driverId;
        grossFreight = TripMoney.money(request.grossFreight(), "grossFreight");
        currency = request.currency();
        advanceAmount = request.advanceAmount() == null ? null : TripMoney.money(request.advanceAmount(), "advanceAmount");
        balanceDueDays = request.balanceDueDays();
        cargoDescription = blankToNull(request.cargoDescription());
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

    BigDecimal getGrossFreight() {
        return grossFreight;
    }

    String getCurrency() {
        return currency;
    }

    BigDecimal getAdvanceAmount() {
        return advanceAmount;
    }

    Integer getBalanceDueDays() {
        return balanceDueDays;
    }

    String getCargoDescription() {
        return cargoDescription;
    }
}
