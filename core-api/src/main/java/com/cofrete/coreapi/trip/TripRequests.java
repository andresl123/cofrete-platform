package com.cofrete.coreapi.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

record RoutePointRequest(
    @NotBlank @Size(max = 120) String city,
    @NotBlank @Pattern(regexp = "[A-Z]{2}") String state
) {
}

record CreateTripRequest(
    @NotBlank @Size(max = 64) String truckId,
    @NotNull @Valid RoutePointRequest origin,
    @NotNull @Valid RoutePointRequest destination,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 12, fraction = 2) BigDecimal loadedKm,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal emptyKm,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 12, fraction = 2) BigDecimal grossFreight,
    @NotBlank @Pattern(regexp = "BRL") String currency,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal advanceAmount,
    @Min(0) @Max(365) Integer balanceDueDays,
    @Size(max = 240) String cargoDescription,
    Instant expectedPickupAt
) {
}

record ReservePolicyRequest(
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal maintenanceRate,
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal tireRate,
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal taxRate,
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal insuranceRate,
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal replacementRate,
    @NotNull @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal emergencyRate
) {
}

record ProfitabilityEstimateRequest(
    @NotNull @DecimalMin(value = "0.0000", inclusive = false) @Digits(integer = 8, fraction = 4)
    BigDecimal dieselConsumptionKmPerLiter,
    @NotNull @DecimalMin("0.0000") @Digits(integer = 8, fraction = 4) BigDecimal dieselPricePerLiter,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal arlaCost,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal nonReimbursedToll,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal tollReimbursement,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal valePedagio,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal otherPassThrough,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal mealsAndLodgingCost,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal otherDirectCost,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal financingAllocation,
    @NotNull @Valid ReservePolicyRequest reservePolicy,
    Map<String, String> sourceFreshness
) {

    String normalizedSourceFreshness() {
        Map<String, String> normalized = new TreeMap<>();
        normalized.put("compliance", "UNKNOWN");
        normalized.put("fuel", "UNKNOWN");
        normalized.put("tax", "UNKNOWN");
        normalized.put("toll", "UNKNOWN");
        if (sourceFreshness != null) {
            sourceFreshness.forEach((key, value) -> {
                if (key != null && value != null) {
                    normalized.put(key.trim(), value.trim().toUpperCase());
                }
            });
        }
        return normalized.toString();
    }
}

record AcceptanceDecisionRequest(
    @NotNull AcceptanceDecisionValue decision,
    @Size(max = 10) List<@NotBlank @Size(max = 60) String> reasonCodes,
    @Size(max = 500) String note,
    Instant decidedAt
) {

    AcceptanceDecisionRequest {
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
    }
}
