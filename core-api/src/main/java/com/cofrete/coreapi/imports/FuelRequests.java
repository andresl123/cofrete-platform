package com.cofrete.coreapi.imports;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

record DriverFuelReportRequest(
    @NotBlank @Size(max = 40) String fuelType,
    @NotBlank @Pattern(regexp = "[A-Z]{2}") String state,
    @Size(max = 120) String city,
    @Size(max = 160) String stationName,
    @NotNull @DecimalMin(value = "0.0000", inclusive = false) @Digits(integer = 6, fraction = 4) BigDecimal pricePerLiter,
    @NotBlank @Pattern(regexp = "BRL") String currency,
    @Size(max = 160) String receiptReference,
    Instant reportedAt
) {
}

record FuelEstimateRequest(
    @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 12, fraction = 2) BigDecimal routeKm,
    @Size(max = 20) String loadStatus,
    @DecimalMin(value = "0.0000", inclusive = false) @Digits(integer = 8, fraction = 4) BigDecimal consumptionKmPerLiter,
    @DecimalMin(value = "0.0000", inclusive = false) @Digits(integer = 6, fraction = 4) BigDecimal pricePerLiterOverride,
    @DecimalMin("0.00") @Digits(integer = 5, fraction = 2) BigDecimal safetyMarginPercent
) {
}
