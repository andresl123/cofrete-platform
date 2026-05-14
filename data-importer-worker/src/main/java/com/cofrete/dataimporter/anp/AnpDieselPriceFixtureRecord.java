package com.cofrete.dataimporter.anp;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AnpDieselPriceFixtureRecord(
    String fuelType,
    String state,
    String city,
    BigDecimal pricePerLiterBrl,
    LocalDate periodStart,
    LocalDate periodEnd,
    String sourceUrl,
    FreshnessStatus freshnessStatus,
    String confidence
) {
}
