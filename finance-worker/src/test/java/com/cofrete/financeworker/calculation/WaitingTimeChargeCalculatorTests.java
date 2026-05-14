package com.cofrete.financeworker.calculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class WaitingTimeChargeCalculatorTests {

    private final WaitingTimeChargeCalculator calculator = new WaitingTimeChargeCalculator();

    @Test
    void calculatesAdvisoryChargeFromSourceBackedRule() {
        WaitingTimeRuleSnapshot rule = new WaitingTimeRuleSnapshot(
            new BigDecimal("5.00"),
            new BigDecimal("2.34"),
            "BRL",
            "https://www.gov.br/transportes",
            "CURRENT",
            "MEDIUM"
        );

        WaitingTimeChargeResult result = calculator.calculate(
            rule,
            new BigDecimal("8.50"),
            new BigDecimal("27.25")
        );

        assertThat(result.excessHours()).isEqualTo("3.50");
        assertThat(result.advisoryCharge()).isEqualTo("223.18");
        assertThat(result.sourceUrl()).isEqualTo("https://www.gov.br/transportes");
        assertThat(result.advisoryText()).isEqualTo(
            "Waiting-time impact is an advisory estimate. Confirm official rules and contract terms before charging or disputing a customer.");
    }

    @Test
    void returnsZeroChargeWhenThresholdIsNotExceededAndRejectsInvalidInputs() {
        WaitingTimeRuleSnapshot rule = new WaitingTimeRuleSnapshot(
            new BigDecimal("5.00"),
            new BigDecimal("2.34"),
            "BRL",
            "https://www.gov.br/transportes",
            "STALE",
            "LOW"
        );

        WaitingTimeChargeResult result = calculator.calculate(rule, new BigDecimal("4.75"), new BigDecimal("20.00"));

        assertThat(result.excessHours()).isEqualTo("0.00");
        assertThat(result.advisoryCharge()).isEqualTo("0.00");
        assertThat(result.ruleStatus()).isEqualTo("STALE");

        assertThatThrownBy(() -> calculator.calculate(rule, new BigDecimal("-0.01"), new BigDecimal("20.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("waitedHours must be non-negative.");
    }
}
