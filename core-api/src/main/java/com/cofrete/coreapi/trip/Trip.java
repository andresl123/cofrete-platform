package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trips")
class Trip {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(nullable = false, length = 64)
    private String truckId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freight_id", nullable = false)
    private Freight freight;

    @Column(nullable = false, length = 120)
    private String originCity;

    @Column(nullable = false, length = 2)
    private String originState;

    @Column(nullable = false, length = 120)
    private String destinationCity;

    @Column(nullable = false, length = 2)
    private String destinationState;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal loadedKm;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal emptyKm;

    private Instant expectedPickupAt;

    @Column(nullable = false)
    private int inputRevision = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TripDecisionState decisionState = TripDecisionState.DRAFT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_snapshot_id")
    private ProfitabilitySnapshot latestSnapshot;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Trip() {
    }

    Trip(String accountId, String driverId, String truckId, Freight freight, CreateTripRequest request) {
        id = DomainIds.prefixed("trip");
        this.accountId = accountId;
        this.driverId = driverId;
        this.truckId = truckId;
        this.freight = freight;
        originCity = request.origin().city().trim();
        originState = request.origin().state().toUpperCase();
        destinationCity = request.destination().city().trim();
        destinationState = request.destination().state().toUpperCase();
        loadedKm = TripMoney.distance(request.loadedKm(), "loadedKm");
        emptyKm = TripMoney.distance(request.emptyKm(), "emptyKm");
        expectedPickupAt = request.expectedPickupAt();
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

    int nextInputRevision() {
        inputRevision += 1;
        return inputRevision;
    }

    void attachSnapshot(ProfitabilitySnapshot snapshot) {
        latestSnapshot = snapshot;
    }

    void applyDecision(AcceptanceDecisionValue decision) {
        decisionState = switch (decision) {
            case ACCEPT -> TripDecisionState.ACCEPTED;
            case REJECT -> TripDecisionState.REJECTED;
            case RENEGOTIATE -> TripDecisionState.RENEGOTIATION;
        };
    }

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    String getDriverId() {
        return driverId;
    }

    String getTruckId() {
        return truckId;
    }

    Freight getFreight() {
        return freight;
    }

    String getOriginCity() {
        return originCity;
    }

    String getOriginState() {
        return originState;
    }

    String getDestinationCity() {
        return destinationCity;
    }

    String getDestinationState() {
        return destinationState;
    }

    BigDecimal getLoadedKm() {
        return loadedKm;
    }

    BigDecimal getEmptyKm() {
        return emptyKm;
    }

    BigDecimal totalDistanceKm() {
        return loadedKm.add(emptyKm);
    }

    Instant getExpectedPickupAt() {
        return expectedPickupAt;
    }

    int getInputRevision() {
        return inputRevision;
    }

    TripDecisionState getDecisionState() {
        return decisionState;
    }

    ProfitabilitySnapshot getLatestSnapshot() {
        return latestSnapshot;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
