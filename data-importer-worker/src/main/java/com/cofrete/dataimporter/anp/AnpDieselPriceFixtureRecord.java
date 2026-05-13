package com.cofrete.dataimporter.anp;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AnpDieselPriceFixtureRecord(
    String fuelType,
    String state,
    BigDecimal pricePerLiterBrl,
    LocalDate periodStart,
    LocalDate periodEnd,
    FreshnessStatus freshnessStatus,
    String confidence
) {
}
