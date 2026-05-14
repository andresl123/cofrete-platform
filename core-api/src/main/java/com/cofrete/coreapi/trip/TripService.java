package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.auth.AppUser;
import com.cofrete.coreapi.imports.TollDataService;
import com.cofrete.coreapi.imports.TollEstimateCriteria;
import com.cofrete.coreapi.profile.ProfileLookupService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
    private final TripTollRepository tripTolls;
    private final ValePedagioRecordRepository valePedagioRecords;
    private final TripProfitabilityCalculator calculator;
    private final TripRecalculationEventPublisher eventPublisher;
    private final TollDataService tollData;

    TripService(
        ProfileLookupService profileLookup,
        FreightRepository freights,
        TripRepository trips,
        TripCostInputRepository costInputs,
        ProfitabilitySnapshotRepository snapshots,
        TripAcceptanceDecisionRepository decisions,
        TripTollRepository tripTolls,
        ValePedagioRecordRepository valePedagioRecords,
        TripProfitabilityCalculator calculator,
        TripRecalculationEventPublisher eventPublisher,
        TollDataService tollData
    ) {
        this.profileLookup = profileLookup;
        this.freights = freights;
        this.trips = trips;
        this.costInputs = costInputs;
        this.snapshots = snapshots;
        this.decisions = decisions;
        this.tripTolls = tripTolls;
        this.valePedagioRecords = valePedagioRecords;
        this.calculator = calculator;
        this.eventPublisher = eventPublisher;
        this.tollData = tollData;
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
        var input = costInputs.save(new TripCostInput(trip, inputRevision, withPersistedTollDefaults(trip, request)));
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

    @Transactional(readOnly = true)
    TollEstimateResponse estimateTolls(AppUser user, TollEstimateRequest request) {
        if (request.tripId() != null && !request.tripId().isBlank()) {
            requireOwnedTrip(user, request.tripId());
        }
        var importedEstimate = tollData.estimate(new TollEstimateCriteria(
            request.origin().state(),
            request.destination().state(),
            request.distanceKm(),
            request.axles(),
            request.vehicleType()
        ));
        return TollEstimateResponse.from(request, importedEstimate);
    }

    @Transactional(readOnly = true)
    TripTollsEnvelope getTolls(AppUser user, String tripId) {
        var trip = requireOwnedTrip(user, tripId);
        var tolls = tripTolls.findByTripOrderByCreatedAtAsc(trip);
        var valeRecords = valePedagioRecords.findByTripOrderByCreatedAtAsc(trip);
        return new TripTollsEnvelope(
            tolls.stream().map(TripTollResponse::from).toList(),
            valeRecords.stream().map(ValePedagioRecordResponse::from).toList(),
            TollClassificationTotalsResponse.from(classificationTotals(tolls, valeRecords))
        );
    }

    @Transactional
    TripTollResponse recordManualTollPayment(
        AppUser user,
        String tripId,
        ManualTollPaymentRequest request,
        String correlationId
    ) {
        validateCurrency(request.currency());
        validateTollClassificationAmount(request);
        var trip = requireOwnedTrip(user, tripId);
        var toll = tripTolls.save(new TripToll(trip, request));
        trip.nextInputRevision();
        eventPublisher.publish(trip, RecalculationReason.TOLL_CLASSIFICATION_UPDATED, requireCorrelationId(correlationId));
        return TripTollResponse.from(toll);
    }

    @Transactional
    ValePedagioRecordResponse recordValePedagio(
        AppUser user,
        String tripId,
        ValePedagioRequest request,
        String correlationId
    ) {
        validateCurrency(request.currency());
        var trip = requireOwnedTrip(user, tripId);
        var record = valePedagioRecords.save(new ValePedagioRecord(trip, request));
        trip.nextInputRevision();
        eventPublisher.publish(trip, RecalculationReason.TOLL_CLASSIFICATION_UPDATED, requireCorrelationId(correlationId));
        return ValePedagioRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    TollDataImportStatusResponse tollDataImportStatus(String source) {
        return TollDataImportStatusResponse.from(tollData.importStatus(source));
    }

    private Trip requireOwnedTrip(AppUser user, String tripId) {
        var trip = trips.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found."));
        if (!trip.getAccountId().equals(user.getAccountId())) {
            throw new TripForbiddenException("Trip is not visible to this principal.");
        }
        return trip;
    }

    private ProfitabilityEstimateRequest withPersistedTollDefaults(Trip trip, ProfitabilityEstimateRequest request) {
        if (request.nonReimbursedToll() != null && request.tollReimbursement() != null && request.valePedagio() != null) {
            return request;
        }
        var totals = classificationTotals(
            tripTolls.findByTripOrderByCreatedAtAsc(trip),
            valePedagioRecords.findByTripOrderByCreatedAtAsc(trip)
        );
        BigDecimal nonReimbursedToll = request.nonReimbursedToll() == null
            ? totals.driverPaidAndUnknownAmount()
            : request.nonReimbursedToll();
        BigDecimal tollReimbursement = request.tollReimbursement() == null
            ? totals.passThroughAndIncludedInFreightAmount()
            : request.tollReimbursement();
        BigDecimal valePedagio = request.valePedagio() == null
            ? totals.valePedagioPassThroughAmount()
            : request.valePedagio();
        return new ProfitabilityEstimateRequest(
            request.dieselConsumptionKmPerLiter(),
            request.dieselPricePerLiter(),
            request.arlaCost(),
            nonReimbursedToll,
            tollReimbursement,
            valePedagio,
            request.otherPassThrough(),
            request.mealsAndLodgingCost(),
            request.otherDirectCost(),
            request.financingAllocation(),
            request.reservePolicy(),
            request.sourceFreshness()
        );
    }

    private static TollClassificationTotals classificationTotals(
        List<TripToll> tolls,
        List<ValePedagioRecord> valeRecords
    ) {
        BigDecimal passThrough = zero();
        BigDecimal driverPaid = zero();
        BigDecimal included = zero();
        BigDecimal noToll = zero();
        BigDecimal unknown = zero();
        for (TripToll toll : tolls) {
            switch (toll.getClassification()) {
                case PASS_THROUGH -> passThrough = passThrough.add(toll.getAmount());
                case DRIVER_PAID_NON_REIMBURSED -> driverPaid = driverPaid.add(toll.getAmount());
                case INCLUDED_IN_FREIGHT -> included = included.add(toll.getAmount());
                case NO_TOLL -> noToll = noToll.add(toll.getAmount());
                case UNKNOWN -> unknown = unknown.add(toll.getAmount());
            }
        }
        BigDecimal valePassThrough = zero();
        for (ValePedagioRecord record : valeRecords) {
            if (record.getClassification() == TollClassification.PASS_THROUGH) {
                valePassThrough = valePassThrough.add(record.getAmount());
            } else {
                unknown = unknown.add(record.getAmount());
            }
        }
        return new TollClassificationTotals(
            money(passThrough),
            money(driverPaid),
            money(included),
            money(noToll),
            money(unknown),
            money(valePassThrough)
        );
    }

    private static void validateCurrency(String currency) {
        if (!TripMoney.BRL.equals(currency)) {
            throw new TripValidationException("currency must be BRL.");
        }
    }

    private static void validateTollClassificationAmount(ManualTollPaymentRequest request) {
        if (request.classification() == TollClassification.NO_TOLL && request.amount().compareTo(BigDecimal.ZERO) > 0) {
            throw new TripValidationException("NO_TOLL records must use amount 0.00.");
        }
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String requireCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId.trim();
        }
        return "corr_" + UUID.randomUUID().toString().replace("-", "");
    }
}

record TollClassificationTotals(
    BigDecimal passThroughAmount,
    BigDecimal driverPaidNonReimbursedAmount,
    BigDecimal includedInFreightAmount,
    BigDecimal noTollAmount,
    BigDecimal unknownAmount,
    BigDecimal valePedagioPassThroughAmount
) {

    BigDecimal driverPaidAndUnknownAmount() {
        return driverPaidNonReimbursedAmount.add(unknownAmount);
    }

    BigDecimal passThroughAndIncludedInFreightAmount() {
        return passThroughAmount.add(includedInFreightAmount);
    }
}
