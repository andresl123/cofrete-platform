package com.cofrete.coreapi.profile;

import java.math.BigDecimal;

public record ProfileTruckConsumption(
    String driverId,
    String truckId,
    String fuelType,
    BigDecimal loadedAvgKmPerLiter,
    BigDecimal emptyAvgKmPerLiter,
    BigDecimal last30DaysAvgKmPerLiter,
    String confidence
) {
}
