package com.cofrete.financeworker.calculation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TripFinanceCalculatorFixtureTests {

    private final TripFinanceCalculator calculator = new TripFinanceCalculator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void calculatesDeterministicTripFinanceOutputFromFixture() throws IOException {
        CalculationFixture fixture = loadFixture();

        TripFinanceCalculationResult first = calculator.calculate(fixture.input());
        TripFinanceCalculationResult second = calculator.calculate(fixture.input());

        assertThat(second).isEqualTo(first);
        assertThat(first.grossFreight()).isEqualTo(fixture.expected().grossFreight());
        assertThat(first.passThroughAmount()).isEqualTo(fixture.expected().passThroughAmount());
        assertThat(first.directTripCost()).isEqualTo(fixture.expected().directTripCost());
        assertThat(first.requiredReserves()).isEqualTo(fixture.expected().requiredReserves());
        assertThat(first.safePersonalWithdrawal()).isEqualTo(fixture.expected().safePersonalWithdrawal());
        assertThat(first.expectedProfit()).isEqualTo(fixture.expected().expectedProfit());
        assertThat(first.financialHealthImpact().name()).isEqualTo(fixture.expected().financialHealthImpact());
        assertThat(first.calculationTraceId()).startsWith("calc_");
    }

    @Test
    void returnsDecimalStringMonetaryValuesAndTraceOutput() throws IOException {
        TripFinanceCalculationResult result = calculator.calculate(loadFixture().input());

        assertThat(Map.of(
            "grossFreight", result.grossFreight(),
            "passThroughAmount", result.passThroughAmount(),
            "directTripCost", result.directTripCost(),
            "requiredReserves", result.requiredReserves(),
            "safePersonalWithdrawal", result.safePersonalWithdrawal(),
            "expectedProfit", result.expectedProfit()
        )).allSatisfy((field, amount) -> assertThat(amount)
            .as(field)
            .matches("\\d+\\.\\d{2}"));
        assertThat(result.directCostBreakdown()).containsEntry("fuel", "2100.00");
        assertThat(result.reserveBreakdown()).containsEntry("maintenance", "592.00");
        assertThat(result.trace())
            .extracting(CalculationTraceEntry::category)
            .containsExactly(
                "fuel",
                "passThrough",
                "reserveBase",
                "directTripCost",
                "requiredReserves",
                "expectedProfit",
                "safePersonalWithdrawal"
            );
    }

    @Test
    void excludesTollReimbursementAndValePedagioFromProfit() throws IOException {
        TripFinanceInputSnapshot input = loadFixture().input();
        TripFinanceInputSnapshot passThroughHeavy = new TripFinanceInputSnapshot(
            input.tripId(),
            input.driverId(),
            input.truckId(),
            input.inputRevision(),
            input.currency(),
            "1000.00",
            "0.00",
            "1.00",
            "0.00",
            "0.00",
            "0.00",
            "600.00",
            "300.00",
            "0.00",
            "0.00",
            "0.00",
            "0.00",
            new ReservePolicy("0.0000", "0.0000", "0.0000", "0.0000", "0.0000", "0.0000"),
            input.sourceFreshness()
        );

        TripFinanceCalculationResult result = calculator.calculate(passThroughHeavy);

        assertThat(result.passThroughAmount()).isEqualTo("900.00");
        assertThat(result.expectedProfit()).isEqualTo("100.00");
        assertThat(result.safePersonalWithdrawal()).isEqualTo("100.00");
    }

    private CalculationFixture loadFixture() throws IOException {
        return objectMapper.readValue(
            Path.of("src", "test", "resources", "fixtures", "finance-calculation-trip-profit.json").toFile(),
            CalculationFixture.class
        );
    }

    private record CalculationFixture(
        TripFinanceInputSnapshot input,
        ExpectedFinanceOutput expected
    ) {
    }

    private record ExpectedFinanceOutput(
        String grossFreight,
        String passThroughAmount,
        String directTripCost,
        String requiredReserves,
        String safePersonalWithdrawal,
        String expectedProfit,
        String financialHealthImpact
    ) {
    }
}
