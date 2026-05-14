package com.cofrete.coreapi.imports;

import java.math.BigDecimal;

public record TollEstimateCriteria(
    String originState,
    String destinationState,
    BigDecimal distanceKm,
    int axles,
    String vehicleType
) {
}
