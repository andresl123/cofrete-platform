package com.cofrete.financeworker.reserve;

import com.cofrete.financeworker.messaging.FinanceEventNames;
import com.cofrete.financeworker.messaging.events.ReserveAllocationRequestedEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
public class ReserveAllocationService {

    private final ReserveAllocationInputSnapshotProvider snapshots;
    private final ReserveAllocationCalculator calculator;
    private final ProcessedReserveAllocationRegistry processedRegistry;

    public ReserveAllocationService(
        ReserveAllocationInputSnapshotProvider snapshots,
        ReserveAllocationCalculator calculator,
        ProcessedReserveAllocationRegistry processedRegistry
    ) {
        this.snapshots = snapshots;
        this.calculator = calculator;
        this.processedRegistry = processedRegistry;
    }

    public ReserveAllocationResult handle(ReserveAllocationRequestedEvent event) {
        validateEvent(event);
        if (!processedRegistry.markInProgressIfFirst(event.idempotencyKey())) {
            return null;
        }
        try {
            ReserveAllocationResult result = calculator.calculate(snapshots.load(event));
            processedRegistry.markProcessed(event.idempotencyKey());
            return result;
        } catch (RuntimeException | Error exception) {
            processedRegistry.markFailed(event.idempotencyKey());
            throw exception;
        }
    }

    private static void validateEvent(ReserveAllocationRequestedEvent event) {
        Assert.notNull(event, "event is required");
        Assert.isTrue(FinanceEventNames.RESERVE_ALLOCATION_REQUESTED.equals(event.eventType()),
            () -> "Expected eventType " + FinanceEventNames.RESERVE_ALLOCATION_REQUESTED + " but received " + event.eventType());
        Assert.isTrue(event.version() == 1, "reserve.allocation.requested version must be 1");
        Assert.isTrue(StringUtils.hasText(event.eventId()), "eventId is required");
        Assert.isTrue(StringUtils.hasText(event.idempotencyKey()), "idempotencyKey is required");
        Assert.isTrue(StringUtils.hasText(event.allocationSubjectId()), "allocationSubjectId is required");
        Assert.isTrue(event.allocationRevision() > 0, "allocationRevision must be positive");
        Assert.isTrue(StringUtils.hasText(event.driverId()), "driverId is required");
        Assert.isTrue(StringUtils.hasText(event.grossAmount()), "grossAmount is required");
        Assert.isTrue(StringUtils.hasText(event.passThroughAmount()), "passThroughAmount is required");
        Assert.isTrue("BRL".equals(event.currency()), "currency must be BRL");
        Assert.isTrue(StringUtils.hasText(event.reason()), "reason is required");
        Assert.isTrue(StringUtils.hasText(event.correlationId()), "correlationId is required");
        Assert.isTrue("core-api".equals(event.producer()), "producer must be core-api");
        Assert.notNull(event.requestedAt(), "requestedAt is required");
    }
}
