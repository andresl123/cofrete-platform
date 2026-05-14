package com.cofrete.financeworker.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WaitingTimeChargeCalculator {

    private static final String ADVISORY_TEXT =
        "Waiting-time impact is an advisory estimate. Confirm official rules and contract terms before charging or disputing a customer.";

    public WaitingTimeChargeResult calculate(
        WaitingTimeRuleSnapshot rule,
        BigDecimal waitedHours,
        BigDecimal cargoTons
    ) {
        if (rule == null) {
            throw new IllegalArgumentException("rule is required.");
        }
        if (waitedHours == null || waitedHours.signum() < 0) {
            throw new IllegalArgumentException("waitedHours must be non-negative.");
        }
        if (cargoTons == null || cargoTons.signum() < 0) {
            throw new IllegalArgumentException("cargoTons must be non-negative.");
        }

        BigDecimal excessHours = waitedHours.subtract(rule.thresholdHours()).max(BigDecimal.ZERO);
        BigDecimal charge = cargoTons
            .multiply(excessHours)
            .multiply(rule.ratePerTonHour())
            .setScale(2, RoundingMode.HALF_UP);

        return new WaitingTimeChargeResult(
            scale(waitedHours),
            scale(excessHours),
            scale(cargoTons),
            money(rule.ratePerTonHour()),
            money(charge),
            rule.currency(),
            rule.sourceUrl(),
            rule.ruleStatus(),
            rule.confidence(),
            ADVISORY_TEXT
        );
    }

    private static String scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
