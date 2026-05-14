package com.cofrete.coreapi.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

@Component
class TripProfitabilityCalculator {

    private static final BigDecimal GOOD_MARGIN_PERCENT_THRESHOLD = new BigDecimal("10.00");

    ProfitabilityMathResult calculate(Trip trip, TripCostInput input) {
        BigDecimal grossFreight = trip.getFreight().getGrossFreight();
        BigDecimal fuelCost = TripMoney.roundMoney(
            trip.totalDistanceKm()
                .divide(input.getDieselConsumptionKmPerLiter(), 8, RoundingMode.HALF_UP)
                .multiply(input.getDieselPricePerLiter())
        );
        BigDecimal directTripCost = sum(
            fuelCost,
            input.getArlaCost(),
            input.getNonReimbursedToll(),
            input.getMealsAndLodgingCost(),
            input.getOtherDirectCost()
        );
        BigDecimal passThroughAmount = sum(
            input.getTollReimbursement(),
            input.getValePedagio(),
            input.getOtherPassThrough()
        );
        if (passThroughAmount.compareTo(grossFreight) > 0) {
            throw new CalculationUnavailableException("Pass-through amount cannot exceed gross freight.");
        }

        BigDecimal reserveBase = grossFreight.subtract(passThroughAmount);
        BigDecimal requiredReserves = sum(
            reserve(reserveBase, input.getMaintenanceRate()),
            reserve(reserveBase, input.getTireRate()),
            reserve(reserveBase, input.getTaxRate()),
            reserve(reserveBase, input.getInsuranceRate()),
            reserve(reserveBase, input.getReplacementRate()),
            reserve(reserveBase, input.getEmergencyRate())
        );
        BigDecimal expectedProfit = grossFreight
            .subtract(passThroughAmount)
            .subtract(directTripCost)
            .subtract(requiredReserves)
            .subtract(input.getFinancingAllocation());
        BigDecimal safePersonalWithdrawal = expectedProfit.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal marginPercent = grossFreight.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : expectedProfit
                .multiply(new BigDecimal("100"))
                .divide(grossFreight, 2, RoundingMode.HALF_UP);
        FinancialHealthStatus status = healthStatus(expectedProfit, marginPercent);
        return new ProfitabilityMathResult(
            calculationTraceId(trip, input, Map.of(
                "fuelCost", TripMoney.money(fuelCost),
                "directTripCost", TripMoney.money(directTripCost),
                "passThroughAmount", TripMoney.money(passThroughAmount),
                "requiredReserves", TripMoney.money(requiredReserves),
                "expectedProfit", TripMoney.money(expectedProfit)
            )),
            grossFreight,
            passThroughAmount,
            directTripCost,
            requiredReserves,
            safePersonalWithdrawal,
            expectedProfit.setScale(2, RoundingMode.HALF_UP),
            marginPercent,
            status,
            recommendation(status)
        );
    }

    private static BigDecimal reserve(BigDecimal reserveBase, BigDecimal rate) {
        return TripMoney.roundMoney(reserveBase.multiply(rate));
    }

    private static BigDecimal sum(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (BigDecimal value : values) {
            result = result.add(value);
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private static FinancialHealthStatus healthStatus(BigDecimal expectedProfit, BigDecimal marginPercent) {
        if (expectedProfit.compareTo(BigDecimal.ZERO) < 0) {
            return FinancialHealthStatus.RISK;
        }
        if (marginPercent.compareTo(GOOD_MARGIN_PERCENT_THRESHOLD) >= 0) {
            return FinancialHealthStatus.GOOD;
        }
        return FinancialHealthStatus.ATTENTION;
    }

    private static ProfitabilityRecommendation recommendation(FinancialHealthStatus status) {
        return switch (status) {
            case GOOD -> ProfitabilityRecommendation.ACCEPT;
            case ATTENTION, UNKNOWN -> ProfitabilityRecommendation.RENEGOTIATE;
            case RISK -> ProfitabilityRecommendation.REJECT;
        };
    }

    private static String calculationTraceId(Trip trip, TripCostInput input, Map<String, String> outputs) {
        String canonical = String.join("|",
            trip.getId(),
            trip.getDriverId(),
            trip.getTruckId(),
            Integer.toString(input.getInputRevision()),
            trip.getFreight().getCurrency(),
            TripMoney.money(trip.getFreight().getGrossFreight()),
            TripMoney.money(trip.totalDistanceKm()),
            input.getDieselConsumptionKmPerLiter().toPlainString(),
            input.getDieselPricePerLiter().toPlainString(),
            TripMoney.money(input.getArlaCost()),
            TripMoney.money(input.getNonReimbursedToll()),
            TripMoney.money(input.getTollReimbursement()),
            TripMoney.money(input.getValePedagio()),
            TripMoney.money(input.getOtherPassThrough()),
            TripMoney.money(input.getMealsAndLodgingCost()),
            TripMoney.money(input.getOtherDirectCost()),
            TripMoney.money(input.getFinancingAllocation()),
            input.getMaintenanceRate().toPlainString(),
            input.getTireRate().toPlainString(),
            input.getTaxRate().toPlainString(),
            input.getInsuranceRate().toPlainString(),
            input.getReplacementRate().toPlainString(),
            input.getEmergencyRate().toPlainString(),
            input.getSourceFreshness(),
            new TreeMap<>(outputs).toString()
        );
        return "calc_" + sha256Hex(canonical).substring(0, 20);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}

record ProfitabilityMathResult(
    String calculationTraceId,
    BigDecimal grossFreight,
    BigDecimal passThroughAmount,
    BigDecimal directTripCost,
    BigDecimal requiredReserves,
    BigDecimal safePersonalWithdrawal,
    BigDecimal expectedProfit,
    BigDecimal marginPercent,
    FinancialHealthStatus financialHealthStatus,
    ProfitabilityRecommendation recommendation
) {
}
