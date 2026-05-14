package com.cofrete.coreapi.trip;

import java.math.BigDecimal;

public record TripFuelEstimateContext(
    String tripId,
    String truckId,
    String originCity,
    String originState,
    BigDecimal totalDistanceKm
) {
}
