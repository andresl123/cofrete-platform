package com.cofrete.financeworker.calculation;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public record TripFinanceInputSnapshot(
    String tripId,
    String driverId,
    String truckId,
    int inputRevision,
    String currency,
    String grossFreight,
    String totalDistanceKm,
    String dieselConsumptionKmPerLiter,
    String dieselPricePerLiter,
    String arlaCost,
    String nonReimbursedToll,
    String tollReimbursement,
    String valePedagio,
    String otherPassThrough,
    String mealsAndLodgingCost,
    String otherDirectCost,
    String financingAllocation,
    ReservePolicy reservePolicy,
    Map<String, String> sourceFreshness
) {

    public TripFinanceInputSnapshot {
        Assert.isTrue(StringUtils.hasText(tripId), "tripId is required");
        Assert.isTrue(StringUtils.hasText(driverId), "driverId is required");
        Assert.isTrue(StringUtils.hasText(truckId), "truckId is required");
        Assert.isTrue(inputRevision > 0, "inputRevision must be positive");
        Assert.isTrue(MoneyStrings.BRL.equals(currency), "currency must be BRL for MVP calculations");
        Assert.notNull(reservePolicy, "reservePolicy is required");
        Assert.notNull(sourceFreshness, "sourceFreshness is required");
        sourceFreshness = Collections.unmodifiableMap(new TreeMap<>(sourceFreshness));
    }
}
