package com.cofrete.dataimporter.antt;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AnttTollTariffFixtureRecord(
    String plazaExternalId,
    String plazaName,
    String highway,
    String state,
    String municipality,
    BigDecimal kmMarker,
    String direction,
    BigDecimal latitude,
    BigDecimal longitude,
    String vehicleCategory,
    int axleCount,
    BigDecimal amountBrl,
    String currency,
    LocalDate effectiveStart,
    LocalDate effectiveEnd,
    String sourceUrl,
    FreshnessStatus freshnessStatus,
    String confidence
) {
}
