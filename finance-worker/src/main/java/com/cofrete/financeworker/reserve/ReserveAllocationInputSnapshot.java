package com.cofrete.financeworker.reserve;

import java.util.List;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public record ReserveAllocationInputSnapshot(
    String allocationSubjectId,
    int allocationRevision,
    String accountId,
    String driverId,
    String grossAmount,
    String passThroughAmount,
    String distanceKm,
    String currency,
    List<ReserveAllocationRule> rules
) {

    public ReserveAllocationInputSnapshot {
        Assert.isTrue(StringUtils.hasText(allocationSubjectId), "allocationSubjectId is required");
        Assert.isTrue(allocationRevision > 0, "allocationRevision must be positive");
        Assert.isTrue(StringUtils.hasText(accountId), "accountId is required");
        Assert.isTrue(StringUtils.hasText(driverId), "driverId is required");
        Assert.isTrue("BRL".equals(currency), "currency must be BRL for MVP reserve allocation");
        Assert.notNull(rules, "rules are required");
        Assert.notEmpty(rules, "at least one reserve rule is required");
        rules = List.copyOf(rules);
    }
}
