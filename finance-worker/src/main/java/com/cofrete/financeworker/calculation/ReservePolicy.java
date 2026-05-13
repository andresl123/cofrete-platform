package com.cofrete.financeworker.calculation;

public record ReservePolicy(
    String maintenanceRate,
    String tireRate,
    String taxRate,
    String insuranceRate,
    String replacementRate,
    String emergencyRate
) {
}
