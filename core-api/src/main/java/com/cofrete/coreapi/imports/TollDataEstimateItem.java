package com.cofrete.coreapi.imports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TollDataEstimateItem(
    String name,
    String highway,
    String state,
    BigDecimal amount,
    String currency,
    String confidence,
    String source,
    String sourceType,
    LocalDate effectiveStart,
    String importAuditId
) {
}
