package com.cofrete.coreapi.imports;

import java.math.BigDecimal;
import java.util.List;

public record TollDataEstimate(
    List<TollDataEstimateItem> items,
    BigDecimal totalAmount,
    String currency,
    String freshnessStatus,
    String confidence
) {
}
