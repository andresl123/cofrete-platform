package com.cofrete.financeworker.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class TripFinanceCalculator {

    private static final BigDecimal GOOD_MARGIN_THRESHOLD = new BigDecimal("0.10");

    public TripFinanceCalculationResult calculate(TripFinanceInputSnapshot input) {
        Assert.notNull(input, "input is required");

        BigDecimal grossFreight = MoneyStrings.money(input.grossFreight(), "grossFreight");
        BigDecimal totalDistanceKm = MoneyStrings.decimal(input.totalDistanceKm(), "totalDistanceKm");
        BigDecimal consumptionKmPerLiter = MoneyStrings.decimal(
            input.dieselConsumptionKmPerLiter(),
            "dieselConsumptionKmPerLiter"
        );
        Assert.isTrue(consumptionKmPerLiter.compareTo(BigDecimal.ZERO) > 0, "dieselConsumptionKmPerLiter must be positive");
        BigDecimal dieselPricePerLiter = MoneyStrings.decimal(input.dieselPricePerLiter(), "dieselPricePerLiter");

        BigDecimal dieselLiters = totalDistanceKm.divide(
            consumptionKmPerLiter,
            MoneyStrings.CALCULATION_SCALE,
            RoundingMode.HALF_UP
        );
        BigDecimal dieselCost = MoneyStrings.roundMoney(dieselLiters.multiply(dieselPricePerLiter));
        BigDecimal arlaCost = MoneyStrings.money(input.arlaCost(), "arlaCost");
        BigDecimal nonReimbursedToll = MoneyStrings.money(input.nonReimbursedToll(), "nonReimbursedToll");
        BigDecimal mealsAndLodging = MoneyStrings.money(input.mealsAndLodgingCost(), "mealsAndLodgingCost");
        BigDecimal otherDirect = MoneyStrings.money(input.otherDirectCost(), "otherDirectCost");

        Map<String, String> directCostBreakdown = orderedMoneyMap(Map.of(
            "fuel", dieselCost,
            "arla", arlaCost,
            "nonReimbursedToll", nonReimbursedToll,
            "mealsAndLodging", mealsAndLodging,
            "otherDirect", otherDirect
        ));
        BigDecimal directTripCost = sumMoney(directCostBreakdown);

        BigDecimal tollReimbursement = MoneyStrings.money(input.tollReimbursement(), "tollReimbursement");
        BigDecimal valePedagio = MoneyStrings.money(input.valePedagio(), "valePedagio");
        BigDecimal otherPassThrough = MoneyStrings.money(input.otherPassThrough(), "otherPassThrough");
        Map<String, String> passThroughBreakdown = orderedMoneyMap(Map.of(
            "otherPassThrough", otherPassThrough,
            "tollReimbursement", tollReimbursement,
            "valePedagio", valePedagio
        ));
        BigDecimal passThroughAmount = sumMoney(passThroughBreakdown);

        BigDecimal reserveBase = grossFreight.subtract(passThroughAmount);
        Assert.isTrue(reserveBase.compareTo(BigDecimal.ZERO) >= 0, "pass-through amount cannot exceed gross freight");
        Map<String, String> reserveBreakdown = reserveBreakdown(input.reservePolicy(), reserveBase);
        BigDecimal requiredReserves = sumMoney(reserveBreakdown);

        BigDecimal financingAllocation = MoneyStrings.money(input.financingAllocation(), "financingAllocation");
        BigDecimal expectedProfit = grossFreight
            .subtract(passThroughAmount)
            .subtract(directTripCost)
            .subtract(requiredReserves)
            .subtract(financingAllocation);
        BigDecimal safePersonalWithdrawal = expectedProfit.max(BigDecimal.ZERO);

        List<CalculationTraceEntry> trace = trace(
            grossFreight,
            totalDistanceKm,
            consumptionKmPerLiter,
            dieselPricePerLiter,
            dieselLiters,
            passThroughAmount,
            reserveBase,
            directTripCost,
            requiredReserves,
            financingAllocation,
            expectedProfit,
            safePersonalWithdrawal
        );

        String calculationTraceId = calculationTraceId(
            input,
            directCostBreakdown,
            passThroughBreakdown,
            reserveBreakdown,
            expectedProfit
        );

        return new TripFinanceCalculationResult(
            input.tripId(),
            input.driverId(),
            input.inputRevision(),
            calculationTraceId,
            MoneyStrings.moneyString(grossFreight),
            MoneyStrings.moneyString(passThroughAmount),
            MoneyStrings.moneyString(directTripCost),
            MoneyStrings.moneyString(requiredReserves),
            MoneyStrings.moneyString(safePersonalWithdrawal),
            MoneyStrings.moneyString(expectedProfit),
            input.currency(),
            healthImpact(expectedProfit, reserveBase),
            passThroughBreakdown,
            directCostBreakdown,
            reserveBreakdown,
            input.sourceFreshness(),
            trace
        );
    }

    private static Map<String, String> reserveBreakdown(ReservePolicy policy, BigDecimal reserveBase) {
        return orderedMoneyMap(Map.of(
            "maintenance", reserveBase.multiply(MoneyStrings.rate(policy.maintenanceRate(), "maintenanceRate")),
            "tires", reserveBase.multiply(MoneyStrings.rate(policy.tireRate(), "tireRate")),
            "taxesAndDocuments", reserveBase.multiply(MoneyStrings.rate(policy.taxRate(), "taxRate")),
            "insurance", reserveBase.multiply(MoneyStrings.rate(policy.insuranceRate(), "insuranceRate")),
            "truckReplacement", reserveBase.multiply(MoneyStrings.rate(policy.replacementRate(), "replacementRate")),
            "emergency", reserveBase.multiply(MoneyStrings.rate(policy.emergencyRate(), "emergencyRate"))
        ));
    }

    private static Map<String, String> orderedMoneyMap(Map<String, BigDecimal> values) {
        Map<String, String> ordered = new TreeMap<>();
        values.forEach((key, value) -> ordered.put(key, MoneyStrings.moneyString(value)));
        return ordered;
    }

    private static BigDecimal sumMoney(Map<String, String> values) {
        return values.values().stream()
            .map(value -> MoneyStrings.money(value, "breakdown value"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static FinancialHealthImpact healthImpact(BigDecimal expectedProfit, BigDecimal reserveBase) {
        if (reserveBase.compareTo(BigDecimal.ZERO) == 0) {
            return FinancialHealthImpact.UNKNOWN;
        }
        if (expectedProfit.compareTo(BigDecimal.ZERO) < 0) {
            return FinancialHealthImpact.RISK;
        }
        BigDecimal margin = expectedProfit.divide(reserveBase, MoneyStrings.CALCULATION_SCALE, RoundingMode.HALF_UP);
        if (margin.compareTo(GOOD_MARGIN_THRESHOLD) >= 0) {
            return FinancialHealthImpact.GOOD;
        }
        return FinancialHealthImpact.ATTENTION;
    }

    private static List<CalculationTraceEntry> trace(
        BigDecimal grossFreight,
        BigDecimal totalDistanceKm,
        BigDecimal consumptionKmPerLiter,
        BigDecimal dieselPricePerLiter,
        BigDecimal dieselLiters,
        BigDecimal passThroughAmount,
        BigDecimal reserveBase,
        BigDecimal directTripCost,
        BigDecimal requiredReserves,
        BigDecimal financingAllocation,
        BigDecimal expectedProfit,
        BigDecimal safePersonalWithdrawal
    ) {
        List<CalculationTraceEntry> entries = new ArrayList<>();
        entries.add(new CalculationTraceEntry(
            "fuel",
            "totalDistanceKm / dieselConsumptionKmPerLiter * dieselPricePerLiter = "
                + totalDistanceKm.toPlainString() + " / " + consumptionKmPerLiter.toPlainString()
                + " * " + dieselPricePerLiter.toPlainString() + " using "
                + dieselLiters.setScale(4, RoundingMode.HALF_UP).toPlainString() + " liters",
            MoneyStrings.moneyString(dieselLiters.multiply(dieselPricePerLiter))
        ));
        entries.add(new CalculationTraceEntry(
            "passThrough",
            "tollReimbursement + valePedagio + otherPassThrough; excluded from profit",
            MoneyStrings.moneyString(passThroughAmount)
        ));
        entries.add(new CalculationTraceEntry(
            "reserveBase",
            "grossFreight - passThroughAmount = " + MoneyStrings.moneyString(grossFreight)
                + " - " + MoneyStrings.moneyString(passThroughAmount),
            MoneyStrings.moneyString(reserveBase)
        ));
        entries.add(new CalculationTraceEntry(
            "directTripCost",
            "fuel + arla + nonReimbursedToll + mealsAndLodging + otherDirect",
            MoneyStrings.moneyString(directTripCost)
        ));
        entries.add(new CalculationTraceEntry(
            "requiredReserves",
            "reserveBase * configured reserve rates, rounded per bucket",
            MoneyStrings.moneyString(requiredReserves)
        ));
        entries.add(new CalculationTraceEntry(
            "expectedProfit",
            "grossFreight - passThroughAmount - directTripCost - requiredReserves - financingAllocation",
            MoneyStrings.moneyString(expectedProfit)
        ));
        entries.add(new CalculationTraceEntry(
            "safePersonalWithdrawal",
            "max(expectedProfit, 0.00)",
            MoneyStrings.moneyString(safePersonalWithdrawal)
        ));
        return entries;
    }

    private static String calculationTraceId(
        TripFinanceInputSnapshot input,
        Map<String, String> directCostBreakdown,
        Map<String, String> passThroughBreakdown,
        Map<String, String> reserveBreakdown,
        BigDecimal expectedProfit
    ) {
        String canonical = String.join("|",
            input.tripId(),
            input.driverId(),
            input.truckId(),
            Integer.toString(input.inputRevision()),
            input.currency(),
            input.grossFreight(),
            input.totalDistanceKm(),
            input.dieselConsumptionKmPerLiter(),
            input.dieselPricePerLiter(),
            input.arlaCost(),
            input.nonReimbursedToll(),
            input.tollReimbursement(),
            input.valePedagio(),
            input.otherPassThrough(),
            input.mealsAndLodgingCost(),
            input.otherDirectCost(),
            input.financingAllocation(),
            input.reservePolicy().toString(),
            new TreeMap<>(input.sourceFreshness()).toString(),
            directCostBreakdown.toString(),
            passThroughBreakdown.toString(),
            reserveBreakdown.toString(),
            MoneyStrings.moneyString(expectedProfit)
        );
        return "calc_" + sha256Hex(canonical).substring(0, 20);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
