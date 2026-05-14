package com.cofrete.coreapi.profile;

import java.time.Instant;
import java.time.Duration;
import java.time.LocalDate;

record DriverEnvelope(DriverResponse driver) {
}

record TruckEnvelope(TruckResponse truck) {
}

record TrucksEnvelope(Iterable<TruckResponse> trucks) {
}

record TruckConsumptionProfileEnvelope(TruckConsumptionProfileResponse truckConsumptionProfile) {
}

record TaxProfileEnvelope(TaxProfileResponse taxProfile) {
}

record IpvaRuleEnvelope(IpvaRuleResponse ipvaRule) {
}

record TaxRuleYearEnvelope(TaxRuleYearResponse taxRuleYear) {
}

record DriverResponse(
    String id,
    String accountId,
    String name,
    String email,
    String phone,
    String state,
    DocumentType documentType,
    String cpfCnpjMasked,
    String rntrcNumberMasked,
    RntrcCategory rntrcCategory,
    RntrcStatus rntrcStatus,
    String rntrcSource,
    String advisoryText,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {

    static DriverResponse from(Driver driver) {
        return new DriverResponse(
            driver.getId(),
            driver.getUser().getAccountId(),
            driver.getName(),
            driver.getEmail(),
            driver.getPhone(),
            driver.getState(),
            driver.getDocumentType(),
            maskLast4(driver.getCpfCnpjLast4()),
            maskLast4(last4(driver.getRntrcNumber())),
            driver.getRntrcCategory(),
            driver.getRntrcStatus(),
            driver.getRntrcSource(),
            "RNTRC metadata is app-maintained. Confirm official status in ANTT/RNTRC Digital channels.",
            driver.isActive(),
            driver.getCreatedAt(),
            driver.getUpdatedAt()
        );
    }

    private static String last4(String value) {
        if (value == null || value.length() < 4) {
            return null;
        }
        return value.substring(value.length() - 4);
    }

    private static String maskLast4(String last4) {
        return last4 == null ? null : "***" + last4;
    }
}

record TruckConsumptionProfileResponse(
    String truckId,
    String loadedAvgKmPerLiter,
    String emptyAvgKmPerLiter,
    String last30DaysAvgKmPerLiter,
    String confidence,
    String sourceWindowStart,
    String sourceWindowEnd,
    Instant calculatedAt
) {

    static TruckConsumptionProfileResponse from(TruckConsumptionProfile profile) {
        return new TruckConsumptionProfileResponse(
            profile.getTruck().getId(),
            profile.getLoadedAvgKmL().toPlainString(),
            profile.getEmptyAvgKmL().toPlainString(),
            profile.getLast30DaysAvgKmL().toPlainString(),
            profile.getConfidence(),
            profile.getSourceWindowStart() == null ? null : profile.getSourceWindowStart().toString(),
            profile.getSourceWindowEnd() == null ? null : profile.getSourceWindowEnd().toString(),
            profile.getCalculatedAt()
        );
    }

    static TruckConsumptionProfileResponse defaultFor(Truck truck) {
        var loaded = ProfileService.defaultLoadedConsumption(truck);
        var empty = ProfileService.defaultEmptyConsumption(truck);
        return new TruckConsumptionProfileResponse(
            truck.getId(),
            loaded.toPlainString(),
            empty.toPlainString(),
            loaded.toPlainString(),
            "default_by_fuel_type",
            null,
            null,
            truck.getUpdatedAt()
        );
    }
}

record TruckResponse(
    String id,
    String driverId,
    String plateMasked,
    String renavamMasked,
    String state,
    int axleCount,
    VehicleType vehicleType,
    FuelType fuelType,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {

    static TruckResponse from(Truck truck) {
        return new TruckResponse(
            truck.getId(),
            truck.getDriver().getId(),
            maskPlate(truck.getPlate()),
            truck.getRenavamLast4() == null ? null : "***" + truck.getRenavamLast4(),
            truck.getState(),
            truck.getAxleCount(),
            truck.getVehicleType(),
            truck.getFuelType(),
            truck.isActive(),
            truck.getCreatedAt(),
            truck.getUpdatedAt()
        );
    }

    private static String maskPlate(String plate) {
        if (plate == null || plate.length() < 3) {
            return null;
        }
        return "***" + plate.substring(plate.length() - 3);
    }
}

record TaxProfileResponse(
    String id,
    String driverId,
    TaxRegime regime,
    int planningYear,
    String annualGrossLimit,
    String currency,
    String source,
    String sourceType,
    Instant reviewedAt,
    FreshnessStatus freshnessStatus,
    String advisoryText
) {

    static TaxProfileResponse from(TaxProfile taxProfile) {
        return new TaxProfileResponse(
            taxProfile.getId(),
            taxProfile.getDriver().getId(),
            taxProfile.getRegime(),
            taxProfile.getPlanningYear(),
            taxProfile.getAnnualGrossLimit() == null ? null : taxProfile.getAnnualGrossLimit().toPlainString(),
            taxProfile.getCurrency(),
            taxProfile.getSource(),
            taxProfile.getSourceType(),
            taxProfile.getReviewedAt(),
            taxProfile.getFreshnessStatus(),
            "Tax values are planning metadata. Confirm obligations with Receita Federal or a qualified accountant."
        );
    }
}

record IpvaRuleResponse(
    String id,
    String state,
    VehicleType vehicleType,
    int effectiveYear,
    String ratePercent,
    String currency,
    String sourceUrl,
    Instant reviewedAt,
    RuleStatus ipvaStatus,
    String licensingSourceUrl,
    Instant licensingReviewedAt,
    RuleStatus licensingStatus,
    String advisoryText,
    Instant createdAt,
    Instant updatedAt
) {

    static IpvaRuleResponse from(IpvaRule rule) {
        return new IpvaRuleResponse(
            rule.getId(),
            rule.getState(),
            rule.getVehicleType(),
            rule.getEffectiveYear(),
            rule.getRatePercent() == null ? null : rule.getRatePercent().toPlainString(),
            rule.getCurrency(),
            rule.getSourceUrl(),
            rule.getReviewedAt(),
            SourceRuleStatus.ruleStatus(rule.getFreshnessStatus(), rule.getReviewedAt(), rule.getSourceUrl()),
            rule.getLicensingSourceUrl(),
            rule.getLicensingReviewedAt(),
            SourceRuleStatus.ruleStatus(rule.getLicensingFreshnessStatus(), rule.getLicensingReviewedAt(), rule.getLicensingSourceUrl()),
            "IPVA and licensing reminders are advisory. Confirm official values, due dates, and payment channels with the state DETRAN or SEFAZ.",
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }

    static IpvaRuleResponse missing(String state, VehicleType vehicleType, int effectiveYear) {
        return new IpvaRuleResponse(
            null,
            state,
            vehicleType,
            effectiveYear,
            null,
            "BRL",
            null,
            null,
            RuleStatus.MISSING,
            null,
            null,
            RuleStatus.MISSING,
            "No source-backed state rule is configured. Do not estimate IPVA or licensing from a national hardcoded percentage.",
            null,
            null
        );
    }
}

record TaxRuleYearResponse(
    String id,
    TaxRegime regime,
    int planningYear,
    String annualGrossLimit,
    String currency,
    String formulaMetadata,
    String cargoTransportTaxablePercent,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    String sourceUrl,
    Instant reviewedAt,
    RuleStatus ruleStatus,
    String advisoryText,
    Instant createdAt,
    Instant updatedAt
) {

    static TaxRuleYearResponse from(TaxRuleYear rule) {
        return new TaxRuleYearResponse(
            rule.getId(),
            rule.getRegime(),
            rule.getPlanningYear(),
            rule.getAnnualGrossLimit() == null ? null : rule.getAnnualGrossLimit().toPlainString(),
            rule.getCurrency(),
            rule.getFormulaMetadata(),
            rule.getCargoTransportTaxablePercent() == null ? null : rule.getCargoTransportTaxablePercent().toPlainString(),
            rule.getEffectiveFrom(),
            rule.getEffectiveTo(),
            rule.getSourceUrl(),
            rule.getReviewedAt(),
            SourceRuleStatus.ruleStatus(rule.getFreshnessStatus(), rule.getReviewedAt(), rule.getSourceUrl()),
            "Tax outputs are planning estimates. Confirm Receita Federal, state, municipal, and accountant guidance before filing or changing regime.",
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }

    static TaxRuleYearResponse missing(TaxRegime regime, int planningYear) {
        return new TaxRuleYearResponse(
            null,
            regime,
            planningYear,
            null,
            "BRL",
            null,
            null,
            null,
            null,
            null,
            null,
            RuleStatus.MISSING,
            "No source-backed tax rule is configured for this regime and year. Keep tax reserve output conservative.",
            null,
            null
        );
    }
}

final class SourceRuleStatus {

    private SourceRuleStatus() {
    }

    static RuleStatus ruleStatus(FreshnessStatus freshnessStatus, Instant reviewedAt, String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return RuleStatus.MISSING;
        }
        if (reviewedAt == null) {
            return RuleStatus.UNKNOWN;
        }
        if (reviewedAt.isBefore(Instant.now().minus(Duration.ofDays(370)))) {
            return RuleStatus.STALE;
        }
        if (freshnessStatus == FreshnessStatus.STALE || freshnessStatus == FreshnessStatus.FAILED) {
            return RuleStatus.STALE;
        }
        if (freshnessStatus == FreshnessStatus.CURRENT) {
            return RuleStatus.CURRENT;
        }
        return RuleStatus.UNKNOWN;
    }
}
