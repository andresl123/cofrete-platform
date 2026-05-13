package com.cofrete.financeworker.calculation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record TripFinanceCalculationResult(
    String tripId,
    String driverId,
    int inputRevision,
    String calculationTraceId,
    String grossFreight,
    String passThroughAmount,
    String directTripCost,
    String requiredReserves,
    String safePersonalWithdrawal,
    String expectedProfit,
    String currency,
    FinancialHealthImpact financialHealthImpact,
    Map<String, String> passThroughBreakdown,
    Map<String, String> directCostBreakdown,
    Map<String, String> reserveBreakdown,
    Map<String, String> sourceFreshness,
    List<CalculationTraceEntry> trace
) {

    public TripFinanceCalculationResult {
        passThroughBreakdown = Collections.unmodifiableMap(new TreeMap<>(passThroughBreakdown));
        directCostBreakdown = Collections.unmodifiableMap(new TreeMap<>(directCostBreakdown));
        reserveBreakdown = Collections.unmodifiableMap(new TreeMap<>(reserveBreakdown));
        sourceFreshness = Collections.unmodifiableMap(new TreeMap<>(sourceFreshness));
        trace = List.copyOf(trace);
    }
}
