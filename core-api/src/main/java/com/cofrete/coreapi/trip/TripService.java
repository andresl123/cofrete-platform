package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.auth.AppUser;
import com.cofrete.coreapi.profile.ProfileLookupService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TripService {

    private final ProfileLookupService profileLookup;
    private final FreightRepository freights;
    private final TripRepository trips;
    private final TripCostInputRepository costInputs;
    private final ProfitabilitySnapshotRepository snapshots;
    private final TripAcceptanceDecisionRepository decisions;
    private final TripProfitabilityCalculator calculator;
    private final TripRecalculationEventPublisher eventPublisher;

    TripService(
        ProfileLookupService profileLookup,
        FreightRepository freights,
        TripRepository trips,
        TripCostInputRepository costInputs,
        ProfitabilitySnapshotRepository snapshots,
        TripAcceptanceDecisionRepository decisions,
        TripProfitabilityCalculator calculator,
        TripRecalculationEventPublisher eventPublisher
    ) {
        this.profileLookup = profileLookup;
        this.freights = freights;
        this.trips = trips;
        this.costInputs = costInputs;
        this.snapshots = snapshots;
        this.decisions = decisions;
        this.calculator = calculator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    TripResponse createTrip(AppUser user, CreateTripRequest request, String correlationId) {
        validateCurrency(request.currency());
        var owner = profileLookup.requireTripOwner(user, request.truckId());
        var freight = freights.save(new Freight(user.getAccountId(), owner.driverId(), request));
        var trip = trips.save(new Trip(user.getAccountId(), owner.driverId(), owner.truckId(), freight, request));
        eventPublisher.publish(trip, RecalculationReason.TRIP_CREATED, requireCorrelationId(correlationId));
        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    TripResponse getTrip(AppUser user, String tripId) {
        return TripResponse.from(requireOwnedTrip(user, tripId));
    }

    @Transactional
    ProfitabilityEstimateResponse estimate(AppUser user, String tripId, ProfitabilityEstimateRequest request, String correlationId) {
        var trip = requireOwnedTrip(user, tripId);
        int inputRevision = trip.nextInputRevision();
        var input = costInputs.save(new TripCostInput(trip, inputRevision, request));
        var result = calculator.calculate(trip, input);
        var snapshot = snapshots.save(new ProfitabilitySnapshot(trip, input, result));
        trip.attachSnapshot(snapshot);
        eventPublisher.publish(trip, RecalculationReason.MANUAL_RECALCULATION, requireCorrelationId(correlationId));
        return ProfitabilityEstimateResponse.from(snapshot);
    }

    @Transactional(readOnly = true)
    ProfitabilitySnapshotResponse latestSnapshot(AppUser user, String tripId) {
        var trip = requireOwnedTrip(user, tripId);
        var snapshot = snapshots.findFirstByTripOrderByInputRevisionDesc(trip)
            .orElseThrow(() -> new SnapshotPendingException("Profitability snapshot is pending."));
        return ProfitabilitySnapshotResponse.from(snapshot);
    }

    @Transactional
    AcceptanceDecisionResponse decide(AppUser user, String tripId, AcceptanceDecisionRequest request) {
        var trip = requireOwnedTrip(user, tripId);
        if (trip.getDecisionState() != TripDecisionState.DRAFT) {
            throw new TripConflictException("Trip acceptance decision has already been recorded.");
        }
        var decision = decisions.save(new TripAcceptanceDecision(trip, request));
        trip.applyDecision(request.decision());
        return AcceptanceDecisionResponse.from(trip.getId(), decision);
    }

    private Trip requireOwnedTrip(AppUser user, String tripId) {
        var trip = trips.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found."));
        if (!trip.getAccountId().equals(user.getAccountId())) {
            throw new TripForbiddenException("Trip is not visible to this principal.");
        }
        return trip;
    }

    private static void validateCurrency(String currency) {
        if (!TripMoney.BRL.equals(currency)) {
            throw new TripValidationException("currency must be BRL.");
        }
    }

    private static String requireCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId.trim();
        }
        return "corr_" + UUID.randomUUID().toString().replace("-", "");
    }
}
