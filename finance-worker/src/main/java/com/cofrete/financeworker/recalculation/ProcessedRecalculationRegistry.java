package com.cofrete.financeworker.recalculation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class ProcessedRecalculationRegistry {

    private final Set<String> processedIdempotencyKeys = ConcurrentHashMap.newKeySet();

    public boolean hasProcessed(String idempotencyKey) {
        Assert.isTrue(StringUtils.hasText(idempotencyKey), "idempotencyKey is required");
        return processedIdempotencyKeys.contains(idempotencyKey);
    }

    public void markProcessed(String idempotencyKey) {
        Assert.isTrue(StringUtils.hasText(idempotencyKey), "idempotencyKey is required");
        processedIdempotencyKeys.add(idempotencyKey);
    }
}
