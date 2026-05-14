package com.cofrete.coreapi.receivables;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

record CustomerRequest(
    @NotBlank @Size(max = 160) String name,
    CustomerTaxIdType taxIdType,
    @Pattern(regexp = "\\d{4}") String taxIdLast4,
    @Size(max = 120) String contactName,
    @Size(max = 40) String contactPhone,
    @Min(0) @Max(365) int paymentTermsDays,
    @Size(max = 500) String notes
) {
}

record ReceivableRequest(
    @NotBlank @Size(max = 64) String customerId,
    @Size(max = 64) String tripId,
    @NotNull ReceivableType type,
    @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
    @NotBlank @Pattern(regexp = "BRL") String currency,
    @NotNull LocalDate dueDate,
    @NotNull ReceivablePaymentMethod paymentMethod,
    @Size(max = 500) String note
) {
}

record MarkReceivablePaidRequest(
    @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal paidAmount,
    @NotBlank @Pattern(regexp = "BRL") String currency,
    @NotNull LocalDate paidDate,
    @NotNull ReceivablePaymentMethod paymentMethod,
    @Size(max = 500) String note
) {
}
