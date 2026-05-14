package com.cofrete.financeworker.calculation;

import java.math.BigDecimal;

public record WaitingTimeRuleSnapshot(
    BigDecimal thresholdHours,
    BigDecimal ratePerTonHour,
    String currency,
    String sourceUrl,
    String ruleStatus,
    String confidence
) {

    public WaitingTimeRuleSnapshot {
        if (thresholdHours == null || thresholdHours.signum() < 0) {
            throw new IllegalArgumentException("thresholdHours must be non-negative.");
        }
        if (ratePerTonHour == null || ratePerTonHour.signum() < 0) {
            throw new IllegalArgumentException("ratePerTonHour must be non-negative.");
        }
        if (currency == null || currency.isBlank()) {
            currency = "BRL";
        }
        if (ruleStatus == null || ruleStatus.isBlank()) {
            ruleStatus = "UNKNOWN";
        }
        if (confidence == null || confidence.isBlank()) {
            confidence = "UNKNOWN";
        }
    }
}
