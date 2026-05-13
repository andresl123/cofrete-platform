package com.cofrete.coreapi.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "trucks")
class Truck {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false, length = 12)
    private String plate;

    @Column(length = 4)
    private String renavamLast4;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private int axleCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FuelType fuelType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Truck() {
    }

    Truck(Driver driver, TruckRequest request) {
        id = DomainIds.prefixed("truck");
        this.driver = driver;
        updateFrom(request);
    }

    void updateFrom(TruckRequest request) {
        plate = request.plate().trim().toUpperCase();
        renavamLast4 = request.renavamLast4();
        state = request.state().toUpperCase();
        axleCount = request.axleCount();
        vehicleType = request.vehicleType();
        fuelType = request.fuelType();
        active = request.active() == null || request.active();
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

    String getId() {
        return id;
    }

    Driver getDriver() {
        return driver;
    }

    String getPlate() {
        return plate;
    }

    String getRenavamLast4() {
        return renavamLast4;
    }

    String getState() {
        return state;
    }

    int getAxleCount() {
        return axleCount;
    }

    VehicleType getVehicleType() {
        return vehicleType;
    }

    FuelType getFuelType() {
        return fuelType;
    }

    boolean isActive() {
        return active;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
