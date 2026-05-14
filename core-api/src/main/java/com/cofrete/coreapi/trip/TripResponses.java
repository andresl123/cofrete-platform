package com.cofrete.coreapi.trip;

import java.time.Instant;
import java.util.List;

record TripEnvelope(TripResponse trip) {
}

record ProfitabilityEstimateEnvelope(ProfitabilityEstimateResponse profitabilityEstimate) {
}

record ProfitabilitySnapshotEnvelope(ProfitabilitySnapshotResponse profitabilitySnapshot) {
}

record AcceptanceDecisionEnvelope(AcceptanceDecisionResponse acceptanceDecision) {
}

record RoutePointResponse(String city, String state) {
}

record TripResponse(
    String id,
    String driverId,
    String truckId,
    RoutePointResponse origin,
    RoutePointResponse destination,
    String loadedKm,
    String emptyKm,
    String totalDistanceKm,
    String grossFreight,
    String currency,
    String advanceAmount,
    Integer balanceDueDays,
    String cargoDescription,
    Instant expectedPickupAt,
    int inputRevision,
    TripDecisionState decisionState,
    String latestSnapshotId,
    String profitabilityEstimatePath,
    String profitabilitySnapshotPath,
    Instant createdAt,
    Instant updatedAt
) {

    static TripResponse from(Trip trip) {
        return new TripResponse(
            trip.getId(),
            trip.getDriverId(),
            trip.getTruckId(),
            new RoutePointResponse(trip.getOriginCity(), trip.getOriginState()),
            new RoutePointResponse(trip.getDestinationCity(), trip.getDestinationState()),
            TripMoney.money(trip.getLoadedKm()),
            TripMoney.money(trip.getEmptyKm()),
            TripMoney.money(trip.totalDistanceKm()),
            TripMoney.money(trip.getFreight().getGrossFreight()),
            trip.getFreight().getCurrency(),
            trip.getFreight().getAdvanceAmount() == null ? null : TripMoney.money(trip.getFreight().getAdvanceAmount()),
            trip.getFreight().getBalanceDueDays(),
            trip.getFreight().getCargoDescription(),
            trip.getExpectedPickupAt(),
            trip.getInputRevision(),
            trip.getDecisionState(),
            trip.getLatestSnapshot() == null ? null : trip.getLatestSnapshot().getId(),
            "/api/trips/" + trip.getId() + "/profitability-estimate",
            "/api/trips/" + trip.getId() + "/profitability-snapshot",
            trip.getCreatedAt(),
            trip.getUpdatedAt()
        );
    }
}

record ProfitabilityEstimateResponse(
    String tripId,
    String calculationTraceId,
    String grossFreight,
    String passThroughAmount,
    String directTripCost,
    String requiredReserves,
    String safePersonalWithdrawal,
    String expectedProfit,
    String marginPercent,
    String currency,
    FinancialHealthStatus financialHealthStatus,
    ProfitabilityRecommendation recommendation,
    List<String> caveats,
    List<SourceMetadataResponse> sourceMetadata
) {

    static ProfitabilityEstimateResponse from(ProfitabilitySnapshot snapshot) {
        return new ProfitabilityEstimateResponse(
            snapshot.getTripId(),
            snapshot.getCalculationTraceId(),
            TripMoney.money(snapshot.getGrossFreight()),
            TripMoney.money(snapshot.getPassThroughAmount()),
            TripMoney.money(snapshot.getDirectTripCost()),
            TripMoney.money(snapshot.getRequiredReserves()),
            TripMoney.money(snapshot.getSafePersonalWithdrawal()),
            TripMoney.money(snapshot.getExpectedProfit()),
            TripMoney.percent(snapshot.getMarginPercent()),
            snapshot.getCurrency(),
            snapshot.getFinancialHealthStatus(),
            snapshot.getRecommendation(),
            advisoryCaveats(),
            SourceMetadataResponse.from(snapshot.getSourceFreshness())
        );
    }

    static List<String> advisoryCaveats() {
        return List.of(
            "Toll reimbursement and Vale-Pedagio are tracked as pass-through amounts, not profit.",
            "Profitability is advisory planning output, not legal, tax, accounting, insurance, or government guidance."
        );
    }
}

record ProfitabilitySnapshotResponse(
    String id,
    String tripId,
    int inputRevision,
    String calculationTraceId,
    String grossFreight,
    String passThroughAmount,
    String directTripCost,
    String requiredReserves,
    String safePersonalWithdrawal,
    String expectedProfit,
    String marginPercent,
    String currency,
    FinancialHealthStatus financialHealthStatus,
    ProfitabilityRecommendation recommendation,
    List<String> caveats,
    List<SourceMetadataResponse> sourceMetadata,
    Instant calculatedAt
) {

    static ProfitabilitySnapshotResponse from(ProfitabilitySnapshot snapshot) {
        return new ProfitabilitySnapshotResponse(
            snapshot.getId(),
            snapshot.getTripId(),
            snapshot.getInputRevision(),
            snapshot.getCalculationTraceId(),
            TripMoney.money(snapshot.getGrossFreight()),
            TripMoney.money(snapshot.getPassThroughAmount()),
            TripMoney.money(snapshot.getDirectTripCost()),
            TripMoney.money(snapshot.getRequiredReserves()),
            TripMoney.money(snapshot.getSafePersonalWithdrawal()),
            TripMoney.money(snapshot.getExpectedProfit()),
            TripMoney.percent(snapshot.getMarginPercent()),
            snapshot.getCurrency(),
            snapshot.getFinancialHealthStatus(),
            snapshot.getRecommendation(),
            ProfitabilityEstimateResponse.advisoryCaveats(),
            SourceMetadataResponse.from(snapshot.getSourceFreshness()),
            snapshot.getCreatedAt()
        );
    }

}

record SourceMetadataResponse(String area, String freshnessStatus) {

    static List<SourceMetadataResponse> from(String sourceFreshness) {
        return List.of(
            new SourceMetadataResponse("compliance", freshness(sourceFreshness, "compliance")),
            new SourceMetadataResponse("fuel", freshness(sourceFreshness, "fuel")),
            new SourceMetadataResponse("tax", freshness(sourceFreshness, "tax")),
            new SourceMetadataResponse("toll", freshness(sourceFreshness, "toll"))
        );
    }

    private static String freshness(String sourceFreshness, String area) {
        String prefix = area + "=";
        for (String part : sourceFreshness.replace("{", "").replace("}", "").split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length());
            }
        }
        return "UNKNOWN";
    }
}

record AcceptanceDecisionResponse(
    String id,
    String tripId,
    AcceptanceDecisionValue decision,
    List<String> reasonCodes,
    String note,
    Instant decidedAt,
    Instant createdAt
) {

    static AcceptanceDecisionResponse from(String tripId, TripAcceptanceDecision decision) {
        return new AcceptanceDecisionResponse(
            decision.getId(),
            tripId,
            decision.getDecision(),
            decision.getReasonCodes().isBlank() ? List.of() : List.of(decision.getReasonCodes().split(",")),
            decision.getNote(),
            decision.getDecidedAt(),
            decision.getCreatedAt()
        );
    }
}
