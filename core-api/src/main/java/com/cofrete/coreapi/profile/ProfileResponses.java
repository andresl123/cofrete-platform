package com.cofrete.coreapi.profile;

import java.time.Instant;

record DriverEnvelope(DriverResponse driver) {
}

record TruckEnvelope(TruckResponse truck) {
}

record TrucksEnvelope(Iterable<TruckResponse> trucks) {
}

record TaxProfileEnvelope(TaxProfileResponse taxProfile) {
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
