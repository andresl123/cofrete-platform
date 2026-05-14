package com.cofrete.coreapi.reserve;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ReserveMoney {

    private ReserveMoney() {
    }

    static BigDecimal money(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new ReserveValidationException(fieldName + " is required.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReserveValidationException(fieldName + " must be non-negative.");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ReserveValidationException(fieldName + " must have at most 2 decimal places.");
        }
    }

    static BigDecimal distance(BigDecimal value) {
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ReserveValidationException("distanceKm must have at most 2 decimal places.");
        }
    }

    static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static String optionalMoney(BigDecimal value) {
        return value == null ? null : money(value);
    }

    static String optionalPerKm(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    static String optional(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }
}
