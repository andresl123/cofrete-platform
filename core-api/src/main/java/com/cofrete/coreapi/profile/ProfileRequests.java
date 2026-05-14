package com.cofrete.coreapi.profile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

record CreateDriverRequest(
    @NotBlank @Size(max = 160) String name,
    @Email @Size(max = 320) String email,
    @Size(max = 40) String phone,
    @NotBlank @Pattern(regexp = "[A-Z]{2}") String state,
    DocumentType documentType,
    @Pattern(regexp = "\\d{4}") String cpfCnpjLast4,
    @Size(max = 40) String rntrcNumber,
    RntrcCategory rntrcCategory,
    RntrcStatus rntrcStatus,
    Boolean active
) {
}

record PatchDriverRequest(
    @Size(min = 1, max = 160) String name,
    @Email @Size(max = 320) String email,
    @Size(max = 40) String phone,
    @Pattern(regexp = "[A-Z]{2}") String state,
    DocumentType documentType,
    @Pattern(regexp = "\\d{4}") String cpfCnpjLast4,
    @Size(max = 40) String rntrcNumber,
    RntrcCategory rntrcCategory,
    RntrcStatus rntrcStatus,
    Boolean active
) {
}

record TruckRequest(
    @NotBlank @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String plate,
    @Pattern(regexp = "\\d{4}") String renavamLast4,
    @NotBlank @Pattern(regexp = "[A-Z]{2}") String state,
    @Min(2) @Max(9) int axleCount,
    @NotNull VehicleType vehicleType,
    @NotNull FuelType fuelType,
    Boolean active
) {
}

record TaxProfileRequest(
    @NotNull TaxRegime regime,
    @Min(2020) @Max(2100) int planningYear,
    @DecimalMin(value = "0.00", inclusive = false) BigDecimal annualGrossLimit,
    @Pattern(regexp = "BRL") String currency,
    @Size(max = 160) String source,
    @Size(max = 80) String sourceType,
    Instant reviewedAt,
    FreshnessStatus freshnessStatus
) {
}

record IpvaRuleRequest(
    @NotBlank @Pattern(regexp = "[A-Z]{2}") String state,
    @NotNull VehicleType vehicleType,
    @Min(2020) @Max(2100) int effectiveYear,
    @DecimalMin("0.0000") BigDecimal ratePercent,
    @Pattern(regexp = "BRL") String currency,
    @Size(max = 500) String sourceUrl,
    Instant reviewedAt,
    FreshnessStatus freshnessStatus,
    @Size(max = 500) String licensingSourceUrl,
    Instant licensingReviewedAt,
    FreshnessStatus licensingFreshnessStatus
) {
}

record TaxRuleYearRequest(
    @NotNull TaxRegime regime,
    @Min(2020) @Max(2100) int planningYear,
    @DecimalMin("0.00") BigDecimal annualGrossLimit,
    @Pattern(regexp = "BRL") String currency,
    @Size(max = 2000) String formulaMetadata,
    @DecimalMin("0.0000") @jakarta.validation.constraints.DecimalMax("1.0000") BigDecimal cargoTransportTaxablePercent,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Size(max = 500) String sourceUrl,
    Instant reviewedAt,
    FreshnessStatus freshnessStatus
) {
}
