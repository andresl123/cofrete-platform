package com.cofrete.coreapi.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "truck_consumption_profiles")
class TruckConsumptionProfile {

    @Id
    private String truckId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @Column(name = "loaded_avg_km_l", nullable = false, precision = 8, scale = 4)
    private BigDecimal loadedAvgKmL;

    @Column(name = "empty_avg_km_l", nullable = false, precision = 8, scale = 4)
    private BigDecimal emptyAvgKmL;

    @Column(name = "last_30_days_avg_km_l", nullable = false, precision = 8, scale = 4)
    private BigDecimal last30DaysAvgKmL;

    @Column(nullable = false, length = 80)
    private String confidence;

    private LocalDate sourceWindowStart;

    private LocalDate sourceWindowEnd;

    @Column(nullable = false)
    private Instant calculatedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TruckConsumptionProfile() {
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

    Truck getTruck() {
        return truck;
    }

    BigDecimal getLoadedAvgKmL() {
        return loadedAvgKmL;
    }

    BigDecimal getEmptyAvgKmL() {
        return emptyAvgKmL;
    }

    BigDecimal getLast30DaysAvgKmL() {
        return last30DaysAvgKmL;
    }

    String getConfidence() {
        return confidence;
    }

    LocalDate getSourceWindowStart() {
        return sourceWindowStart;
    }

    LocalDate getSourceWindowEnd() {
        return sourceWindowEnd;
    }

    Instant getCalculatedAt() {
        return calculatedAt;
    }
}
