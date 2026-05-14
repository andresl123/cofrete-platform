package com.cofrete.coreapi.imports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record FuelPriceEnvelope(FuelPriceResponse fuelPrice) {
}

record FuelPricesEnvelope(List<FuelPriceResponse> prices) {
}

record DriverFuelReportEnvelope(DriverFuelReportResponse driverFuelReport) {
}

record FuelEstimateEnvelope(FuelEstimateResponse fuelEstimate) {
}

record FuelPriceResponse(
    String id,
    String fuelType,
    String state,
    String city,
    String pricePerLiter,
    String currency,
    String source,
    String sourceType,
    String sourceUrl,
    String sourcePeriodStart,
    String sourcePeriodEnd,
    Instant retrievedAt,
    String freshnessStatus,
    String confidence,
    String importAuditId
) {

    static FuelPriceResponse from(FuelPrice price) {
        return new FuelPriceResponse(
            price.getId(),
            price.getFuelType(),
            price.getState(),
            price.getCity(),
            FuelResponseFormatting.decimal(price.getPricePerLiter(), 4),
            price.getCurrency(),
            price.getSource(),
            price.getSourceType(),
            price.getSourceUrl(),
            price.getSourcePeriodStart().toString(),
            price.getSourcePeriodEnd().toString(),
            price.getRetrievedAt(),
            price.getFreshnessStatus(),
            price.getConfidence(),
            price.getImportAuditId()
        );
    }
}

record DriverFuelReportResponse(
    String id,
    String fuelType,
    String state,
    String city,
    String stationName,
    String pricePerLiter,
    String currency,
    String source,
    String sourceType,
    String sourceUrl,
    String sourcePeriodStart,
    String sourcePeriodEnd,
    String freshnessStatus,
    String confidence,
    String receiptReference,
    Instant reportedAt,
    Instant createdAt
) {

    static DriverFuelReportResponse from(DriverFuelReport report) {
        return new DriverFuelReportResponse(
            report.getId(),
            report.getFuelType(),
            report.getState(),
            report.getCity(),
            report.getStationName(),
            FuelResponseFormatting.decimal(report.getPricePerLiter(), 4),
            report.getCurrency(),
            report.getSource(),
            report.getSourceType(),
            null,
            null,
            null,
            "CURRENT",
            report.getConfidence(),
            report.getReceiptReference(),
            report.getReportedAt(),
            report.getCreatedAt()
        );
    }
}

record FuelEstimateResponse(
    String tripId,
    String routeKm,
    String truckId,
    String loadStatus,
    String consumptionKmPerLiter,
    String dieselPricePerLiter,
    String estimatedLiters,
    String estimatedFuelCost,
    String safetyMarginPercent,
    String recommendedFuelBudget,
    String currency,
    String priceSource,
    String priceSourceType,
    String priceConfidence,
    String freshnessStatus,
    LocalDate sourcePeriodStart,
    LocalDate sourcePeriodEnd,
    String importAuditId,
    String calculationTraceId
) {
}

final class FuelResponseFormatting {

    private FuelResponseFormatting() {
    }

    static String decimal(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }
}
