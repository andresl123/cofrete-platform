package com.cofrete.financeworker.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

final class MoneyStrings {

    static final String BRL = "BRL";
    static final int MONEY_SCALE = 2;
    static final int CALCULATION_SCALE = 8;

    private MoneyStrings() {
    }

    static BigDecimal money(String value, String fieldName) {
        return nonNegativeDecimal(value, fieldName).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    static BigDecimal decimal(String value, String fieldName) {
        return nonNegativeDecimal(value, fieldName);
    }

    static BigDecimal rate(String value, String fieldName) {
        BigDecimal rate = nonNegativeDecimal(value, fieldName);
        Assert.isTrue(rate.compareTo(BigDecimal.ONE) <= 0, () -> fieldName + " must be less than or equal to 1");
        return rate;
    }

    static String moneyString(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    static BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal nonNegativeDecimal(String value, String fieldName) {
        Assert.isTrue(StringUtils.hasText(value), () -> fieldName + " is required");
        BigDecimal parsed = new BigDecimal(value);
        Assert.isTrue(parsed.compareTo(BigDecimal.ZERO) >= 0, () -> fieldName + " must be non-negative");
        return parsed;
    }
}
