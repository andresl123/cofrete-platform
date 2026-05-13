package com.cofrete.financeworker.recalculation;

import com.cofrete.financeworker.calculation.TripFinanceCalculationResult;
import com.cofrete.financeworker.calculation.TripFinanceCalculator;
import com.cofrete.financeworker.calculation.TripFinanceInputSnapshot;
import com.cofrete.financeworker.messaging.FinanceEventNames;
import com.cofrete.financeworker.messaging.FinanceEventPublisher;
import com.cofrete.financeworker.messaging.events.TripFinanceRecalculatedEvent;
import com.cofrete.financeworker.messaging.events.TripRecalculationRequestedEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
public class TripFinanceRecalculationService {

    private static final String PRODUCER = "finance-worker";

    private final TripFinanceInputSnapshotProvider snapshotProvider;
    private final TripFinanceCalculator calculator;
    private final FinanceEventPublisher publisher;
    private final ProcessedRecalculationRegistry processedRegistry;

    public TripFinanceRecalculationService(
        TripFinanceInputSnapshotProvider snapshotProvider,
        TripFinanceCalculator calculator,
        FinanceEventPublisher publisher,
        ProcessedRecalculationRegistry processedRegistry
    ) {
        this.snapshotProvider = snapshotProvider;
        this.calculator = calculator;
        this.publisher = publisher;
        this.processedRegistry = processedRegistry;
    }

    public boolean handle(TripRecalculationRequestedEvent event) {
        validateEvent(event);
        if (processedRegistry.hasProcessed(event.idempotencyKey())) {
            return false;
        }

        TripFinanceInputRequest request = new TripFinanceInputRequest(
            event.eventId(),
            event.idempotencyKey(),
            event.tripId(),
            event.driverId(),
            event.truckId(),
            event.inputRevision(),
            event.reason(),
            event.correlationId(),
            event.requestedAt()
        );
        TripFinanceInputSnapshot snapshot = snapshotProvider.loadSnapshot(request);
        Assert.isTrue(request.tripId().equals(snapshot.tripId()), "snapshot tripId must match request tripId");
        Assert.isTrue(request.driverId().equals(snapshot.driverId()), "snapshot driverId must match request driverId");
        Assert.isTrue(request.truckId().equals(snapshot.truckId()), "snapshot truckId must match request truckId");
        Assert.isTrue(
            request.inputRevision() == snapshot.inputRevision(),
            "snapshot inputRevision must match request inputRevision"
        );
        TripFinanceCalculationResult result = calculator.calculate(snapshot);
        publisher.publishTripFinanceRecalculated(toEvent(event, result));
        processedRegistry.markProcessed(event.idempotencyKey());
        return true;
    }

    private static void validateEvent(TripRecalculationRequestedEvent event) {
        Assert.notNull(event, "event is required");
        Assert.isTrue(
            FinanceEventNames.TRIP_RECALCULATION_REQUESTED.equals(event.eventType()),
            () -> "Expected eventType " + FinanceEventNames.TRIP_RECALCULATION_REQUESTED
                + " but received " + event.eventType()
        );
        Assert.isTrue(event.version() == 1, "trip.recalculation.requested version must be 1");
        Assert.isTrue(StringUtils.hasText(event.eventId()), "eventId is required");
        Assert.isTrue(StringUtils.hasText(event.idempotencyKey()), "idempotencyKey is required");
        Assert.isTrue(StringUtils.hasText(event.tripId()), "tripId is required");
        Assert.isTrue(StringUtils.hasText(event.driverId()), "driverId is required");
        Assert.isTrue(StringUtils.hasText(event.truckId()), "truckId is required");
        Assert.isTrue(event.inputRevision() > 0, "inputRevision must be positive");
        Assert.isTrue(StringUtils.hasText(event.reason()), "reason is required");
        Assert.isTrue(StringUtils.hasText(event.correlationId()), "correlationId is required");
        Assert.isTrue("core-api".equals(event.producer()), "producer must be core-api");
        Assert.notNull(event.requestedAt(), "requestedAt is required");
    }

    private static TripFinanceRecalculatedEvent toEvent(
        TripRecalculationRequestedEvent request,
        TripFinanceCalculationResult result
    ) {
        String idempotencyKey = "trip:" + result.tripId()
            + ":finance-result:" + result.inputRevision()
            + ":" + result.calculationTraceId();
        String eventId = "evt_" + TripFinanceCalculator.sha256Hex(idempotencyKey + "|" + request.correlationId())
            .substring(0, 20);

        return new TripFinanceRecalculatedEvent(
            FinanceEventNames.TRIP_FINANCE_RECALCULATED,
            1,
            eventId,
            idempotencyKey,
            result.tripId(),
            result.driverId(),
            result.inputRevision(),
            result.calculationTraceId(),
            result.grossFreight(),
            result.passThroughAmount(),
            result.directTripCost(),
            result.requiredReserves(),
            result.safePersonalWithdrawal(),
            result.expectedProfit(),
            result.currency(),
            result.financialHealthImpact().name(),
            result.sourceFreshness(),
            request.correlationId(),
            PRODUCER,
            request.requestedAt()
        );
    }
}
