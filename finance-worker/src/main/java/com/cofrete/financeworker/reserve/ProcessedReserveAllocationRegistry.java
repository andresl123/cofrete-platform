package com.cofrete.financeworker.reserve;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class ProcessedReserveAllocationRegistry {

    private final ConcurrentMap<String, ProcessingState> idempotencyStates = new ConcurrentHashMap<>();

    public boolean markInProgressIfFirst(String idempotencyKey) {
        Assert.isTrue(StringUtils.hasText(idempotencyKey), "idempotencyKey is required");
        return idempotencyStates.putIfAbsent(idempotencyKey, ProcessingState.IN_PROGRESS) == null;
    }

    public void markProcessed(String idempotencyKey) {
        Assert.isTrue(StringUtils.hasText(idempotencyKey), "idempotencyKey is required");
        idempotencyStates.put(idempotencyKey, ProcessingState.PROCESSED);
    }

    public void markFailed(String idempotencyKey) {
        Assert.isTrue(StringUtils.hasText(idempotencyKey), "idempotencyKey is required");
        idempotencyStates.remove(idempotencyKey, ProcessingState.IN_PROGRESS);
    }

    private enum ProcessingState {
        IN_PROGRESS,
        PROCESSED
    }
}
