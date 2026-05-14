package com.cofrete.financeworker.reserve;

import com.cofrete.financeworker.calculation.TripFinanceCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class ReserveAllocationCalculator {

    private static final List<ReserveBucket> REQUIRED_BUCKETS = List.of(
        ReserveBucket.FUEL_ARLA_TOLL_CASH_FLOW,
        ReserveBucket.MAINTENANCE,
        ReserveBucket.TIRES,
        ReserveBucket.INSURANCE,
        ReserveBucket.TAXES_AND_DOCUMENTS,
        ReserveBucket.TRUCK_REPLACEMENT,
        ReserveBucket.EMERGENCY
    );

    public ReserveAllocationResult calculate(ReserveAllocationInputSnapshot input) {
        Assert.notNull(input, "input is required");
        BigDecimal gross = money(input.grossAmount(), "grossAmount");
        BigDecimal passThrough = money(input.passThroughAmount(), "passThroughAmount");
        Assert.isTrue(passThrough.compareTo(gross) <= 0, "passThroughAmount cannot exceed grossAmount");
        BigDecimal allocatable = gross.subtract(passThrough);
        BigDecimal distanceKm = StringUtils.hasText(input.distanceKm())
            ? decimal(input.distanceKm(), "distanceKm")
            : null;

        Map<ReserveBucket, BigDecimal> bucketAmounts = new EnumMap<>(ReserveBucket.class);
        var buckets = new HashSet<ReserveBucket>();
        for (ReserveAllocationRule rule : input.rules()) {
            validateRule(rule, buckets);
            bucketAmounts.put(rule.bucket(), allocationForRule(rule, allocatable, distanceKm));
        }

        BigDecimal totalAllocated = bucketAmounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Assert.isTrue(
            totalAllocated.compareTo(allocatable) <= 0,
            "reserve rules cannot allocate more than allocatableAmount"
        );
        BigDecimal requiredReserves = bucketAmounts.entrySet().stream()
            .filter(entry -> REQUIRED_BUCKETS.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal driverSalary = bucketAmounts.getOrDefault(ReserveBucket.DRIVER_SALARY, BigDecimal.ZERO);
        BigDecimal profit = bucketAmounts.getOrDefault(ReserveBucket.PROFIT, BigDecimal.ZERO);
        BigDecimal safeWithdrawal = driverSalary.compareTo(BigDecimal.ZERO) > 0
            ? driverSalary
            : allocatable.subtract(requiredReserves).subtract(profit).max(BigDecimal.ZERO);
        Map<ReserveBucket, String> roundedBuckets = moneyMap(bucketAmounts);

        String traceInput = String.join("|",
            input.allocationSubjectId(),
            Integer.toString(input.allocationRevision()),
            input.accountId(),
            input.driverId(),
            moneyString(gross),
            moneyString(passThrough),
            moneyString(allocatable),
            roundedBuckets.toString(),
            moneyString(requiredReserves),
            moneyString(safeWithdrawal)
        );

        return new ReserveAllocationResult(
            input.allocationSubjectId(),
            input.allocationRevision(),
            input.accountId(),
            input.driverId(),
            moneyString(gross),
            moneyString(passThrough),
            moneyString(allocatable),
            moneyString(requiredReserves),
            moneyString(safeWithdrawal),
            input.currency(),
            roundedBuckets,
            "reserve_alloc_" + TripFinanceCalculator.sha256Hex(traceInput).substring(0, 20)
        );
    }

    private static BigDecimal allocationForRule(
        ReserveAllocationRule rule,
        BigDecimal allocatable,
        BigDecimal distanceKm
    ) {
        return switch (rule.policy()) {
            case PERCENT_OF_AMOUNT -> roundMoney(allocatable.multiply(rate(rule.rate(), "rate")));
            case FIXED_AMOUNT -> money(rule.fixedAmount(), "fixedAmount");
            case PER_KM -> {
                if (distanceKm == null) {
                    throw new IllegalArgumentException("distanceKm is required for active PER_KM reserve rules");
                }
                yield roundMoney(distanceKm.multiply(decimal(rule.perKmAmount(), "perKmAmount")));
            }
        };
    }

    private static void validateRule(ReserveAllocationRule rule, HashSet<ReserveBucket> buckets) {
        Assert.notNull(rule.bucket(), "reserve rule bucket is required");
        Assert.notNull(rule.policy(), "reserve rule policy is required");
        Assert.isTrue(buckets.add(rule.bucket()), () -> "duplicate reserve rule bucket " + rule.bucket());
        switch (rule.policy()) {
            case PERCENT_OF_AMOUNT -> {
                Assert.isTrue(StringUtils.hasText(rule.rate()), "rate is required");
                Assert.isTrue(!StringUtils.hasText(rule.fixedAmount()) && !StringUtils.hasText(rule.perKmAmount()),
                    "PERCENT_OF_AMOUNT reserve rules only accept rate");
            }
            case FIXED_AMOUNT -> {
                Assert.isTrue(StringUtils.hasText(rule.fixedAmount()), "fixedAmount is required");
                Assert.isTrue(!StringUtils.hasText(rule.rate()) && !StringUtils.hasText(rule.perKmAmount()),
                    "FIXED_AMOUNT reserve rules only accept fixedAmount");
            }
            case PER_KM -> {
                Assert.isTrue(StringUtils.hasText(rule.perKmAmount()), "perKmAmount is required");
                Assert.isTrue(!StringUtils.hasText(rule.rate()) && !StringUtils.hasText(rule.fixedAmount()),
                    "PER_KM reserve rules only accept perKmAmount");
            }
        }
    }

    private static Map<ReserveBucket, String> moneyMap(Map<ReserveBucket, BigDecimal> values) {
        Map<ReserveBucket, String> result = new TreeMap<>();
        values.forEach((bucket, amount) -> result.put(bucket, moneyString(amount)));
        return result;
    }

    private static BigDecimal money(String value, String fieldName) {
        return decimal(value, fieldName).setScale(2, RoundingMode.UNNECESSARY);
    }

    private static BigDecimal rate(String value, String fieldName) {
        BigDecimal parsed = decimal(value, fieldName);
        Assert.isTrue(parsed.compareTo(BigDecimal.ONE) <= 0, () -> fieldName + " must be less than or equal to 1");
        return parsed;
    }

    private static BigDecimal decimal(String value, String fieldName) {
        Assert.isTrue(StringUtils.hasText(value), () -> fieldName + " is required");
        BigDecimal parsed = new BigDecimal(value);
        Assert.isTrue(parsed.compareTo(BigDecimal.ZERO) >= 0, () -> fieldName + " must be non-negative");
        return parsed;
    }

    private static BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String moneyString(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
