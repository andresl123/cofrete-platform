package com.cofrete.financeworker.reserve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReserveAllocationCalculatorTests {

    private final ReserveAllocationCalculator calculator = new ReserveAllocationCalculator();

    @Test
    void allocatesReserveBucketsAndSafeWithdrawalDeterministically() {
        var input = input(List.of(
            percent(ReserveBucket.MAINTENANCE, "0.080000"),
            percent(ReserveBucket.TIRES, "0.040000"),
            percent(ReserveBucket.TAXES_AND_DOCUMENTS, "0.030000"),
            percent(ReserveBucket.DRIVER_SALARY, "0.150000"),
            percent(ReserveBucket.PROFIT, "0.050000")
        ));

        ReserveAllocationResult first = calculator.calculate(input);
        ReserveAllocationResult second = calculator.calculate(input);

        assertThat(second).isEqualTo(first);
        assertThat(first.grossAmount()).isEqualTo("8000.00");
        assertThat(first.passThroughAmount()).isEqualTo("600.00");
        assertThat(first.allocatableAmount()).isEqualTo("7400.00");
        assertThat(first.requiredReserveAmount()).isEqualTo("1110.00");
        assertThat(first.safePersonalWithdrawal()).isEqualTo("1110.00");
        assertThat(first.bucketAllocations())
            .containsEntry(ReserveBucket.MAINTENANCE, "592.00")
            .containsEntry(ReserveBucket.TIRES, "296.00")
            .containsEntry(ReserveBucket.TAXES_AND_DOCUMENTS, "222.00")
            .containsEntry(ReserveBucket.DRIVER_SALARY, "1110.00")
            .containsEntry(ReserveBucket.PROFIT, "370.00");
        assertThat(first.allocationTraceId()).startsWith("reserve_alloc_");
    }

    @Test
    void excludesPassThroughFromReserveBaseAndFallbackSafeWithdrawal() {
        var result = calculator.calculate(input(List.of(
            percent(ReserveBucket.MAINTENANCE, "0.100000"),
            percent(ReserveBucket.PROFIT, "0.100000")
        )));

        assertThat(result.allocatableAmount()).isEqualTo("7400.00");
        assertThat(result.requiredReserveAmount()).isEqualTo("740.00");
        assertThat(result.safePersonalWithdrawal()).isEqualTo("5920.00");
    }

    @Test
    void rejectsOverAllocatedRules() {
        var input = input(List.of(
            percent(ReserveBucket.MAINTENANCE, "0.700000"),
            percent(ReserveBucket.TIRES, "0.400000")
        ));

        assertThatThrownBy(() -> calculator.calculate(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserve rules cannot allocate more than allocatableAmount");
    }

    @Test
    void rejectsDuplicateRuleBuckets() {
        var input = input(List.of(
            percent(ReserveBucket.MAINTENANCE, "0.100000"),
            percent(ReserveBucket.MAINTENANCE, "0.200000")
        ));

        assertThatThrownBy(() -> calculator.calculate(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duplicate reserve rule bucket MAINTENANCE");
    }

    @Test
    void rejectsPolicyExtraRuleAmounts() {
        var input = input(List.of(new ReserveAllocationRule(
            ReserveBucket.MAINTENANCE,
            ReserveRulePolicy.PERCENT_OF_AMOUNT,
            "0.100000",
            "25.00",
            null
        )));

        assertThatThrownBy(() -> calculator.calculate(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PERCENT_OF_AMOUNT reserve rules only accept rate");
    }

    @Test
    void requiresDistanceForPerKmRules() {
        var input = input(List.of(new ReserveAllocationRule(
            ReserveBucket.MAINTENANCE,
            ReserveRulePolicy.PER_KM,
            null,
            null,
            "0.2500"
        )));

        assertThatThrownBy(() -> calculator.calculate(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("distanceKm is required for active PER_KM reserve rules");
    }

    private static ReserveAllocationInputSnapshot input(List<ReserveAllocationRule> rules) {
        return new ReserveAllocationInputSnapshot(
            "pay_123",
            1,
            "driver_123",
            "8000.00",
            "600.00",
            null,
            "BRL",
            rules
        );
    }

    private static ReserveAllocationRule percent(ReserveBucket bucket, String rate) {
        return new ReserveAllocationRule(bucket, ReserveRulePolicy.PERCENT_OF_AMOUNT, rate, null, null);
    }
}
