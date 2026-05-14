package com.cofrete.coreapi.receivables;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ReceivableMoney {

    static final String BRL = "BRL";

    private ReceivableMoney() {
    }

    static BigDecimal money(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new ReceivableValidationException(fieldName + " is required.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReceivableValidationException(fieldName + " must be non-negative.");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ReceivableValidationException(fieldName + " must have at most 2 decimal places.");
        }
    }

    static BigDecimal positiveMoney(BigDecimal value, String fieldName) {
        BigDecimal money = money(value, fieldName);
        if (money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReceivableValidationException(fieldName + " must be greater than zero.");
        }
        return money;
    }

    static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
