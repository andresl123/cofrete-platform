package com.cofrete.coreapi.reserve;

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
import java.time.LocalDate;

record ReserveRuleRequest(
    @NotNull ReserveBucket bucket,
    @NotNull ReserveRulePolicy policy,
    @DecimalMin("0.000000") @Digits(integer = 1, fraction = 6) BigDecimal rate,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal fixedAmount,
    @DecimalMin("0.0000") @Digits(integer = 10, fraction = 4) BigDecimal perKmAmount,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal targetBalance,
    @Pattern(regexp = "BRL") String currency,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    Boolean active,
    @Size(max = 240) String sourceAssumption
) {
}

record ReserveAllocationRequest(
    @Size(max = 64) String tripId,
    @Size(max = 64) String freightPaymentId,
    @NotBlank @Size(max = 64) String allocationSubjectId,
    @Min(1) @Max(999999) int allocationRevision,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal grossAmount,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal passThroughAmount,
    @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal distanceKm,
    @NotBlank @Pattern(regexp = "BRL") String currency,
    @NotBlank @Size(max = 160) String idempotencyKey,
    @NotNull ReserveAllocationReason reason,
    Instant requestedAt
) {
}
