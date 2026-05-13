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

@Entity
@Table(name = "ipva_rules")
class IpvaRule {

    @Id
    private String id;

    @Column(nullable = false, length = 2)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private int effectiveYear;

    @Column(precision = 7, scale = 4)
    private BigDecimal ratePercent;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String sourceUrl;

    private Instant reviewedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected IpvaRule() {
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
