package com.cofrete.financeworker.calculation;

public record WaitingTimeChargeResult(
    String waitedHours,
    String excessHours,
    String cargoTons,
    String ratePerTonHour,
    String advisoryCharge,
    String currency,
    String sourceUrl,
    String ruleStatus,
    String confidence,
    String advisoryText
) {
}
