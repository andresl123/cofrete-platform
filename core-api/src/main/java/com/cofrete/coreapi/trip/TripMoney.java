package com.cofrete.coreapi.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class TripMoney {

    static final String BRL = "BRL";

    private TripMoney() {
    }

    static BigDecimal money(BigDecimal value, String field) {
        if (value == null) {
            throw new TripValidationException(field + " is required.");
        }
        if (value.scale() > 2) {
            throw new TripValidationException(field + " must use at most 2 decimal places.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal optionalMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : money(value, "money");
    }

    static BigDecimal distance(BigDecimal value, String field) {
        if (value == null) {
            throw new TripValidationException(field + " is required.");
        }
        if (value.scale() > 2) {
            throw new TripValidationException(field + " must use at most 2 decimal places.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal rate(BigDecimal value, String field) {
        if (value == null) {
            throw new TripValidationException(field + " is required.");
        }
        if (value.scale() > 6) {
            throw new TripValidationException(field + " must use at most 6 decimal places.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new TripValidationException(field + " must be between 0 and 1.");
        }
        return value;
    }

    static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static String percent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
