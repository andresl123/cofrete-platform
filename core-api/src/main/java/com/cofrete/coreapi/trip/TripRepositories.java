package com.cofrete.coreapi.trip;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface FreightRepository extends JpaRepository<Freight, String> {
}

interface TripRepository extends JpaRepository<Trip, String> {
}

interface TripCostInputRepository extends JpaRepository<TripCostInput, String> {

    Optional<TripCostInput> findFirstByTripOrderByInputRevisionDesc(Trip trip);
}

interface ProfitabilitySnapshotRepository extends JpaRepository<ProfitabilitySnapshot, String> {

    Optional<ProfitabilitySnapshot> findFirstByTripOrderByInputRevisionDesc(Trip trip);
}

interface TripAcceptanceDecisionRepository extends JpaRepository<TripAcceptanceDecision, String> {
}

interface TripRecalculationEventRepository extends JpaRepository<TripRecalculationEventRecord, String> {

    List<TripRecalculationEventRecord> findByTripIdOrderByInputRevisionAsc(String tripId);
}
