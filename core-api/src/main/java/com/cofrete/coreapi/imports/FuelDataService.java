package com.cofrete.coreapi.imports;

import com.cofrete.coreapi.auth.AppUser;
import com.cofrete.coreapi.profile.ProfileDriverOwner;
import com.cofrete.coreapi.profile.ProfileLookupService;
import com.cofrete.coreapi.profile.ProfileTruckConsumption;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class FuelDataService {

    private static final String DEFAULT_FUEL_TYPE = "DIESEL_S10";
    private static final BigDecimal DEFAULT_SAFETY_MARGIN = new BigDecimal("10.00");

    private final ProfileLookupService profileLookup;
    private final FuelPriceRepository fuelPrices;
    private final DriverFuelReportRepository driverFuelReports;

    FuelDataService(
        ProfileLookupService profileLookup,
        FuelPriceRepository fuelPrices,
        DriverFuelReportRepository driverFuelReports
    ) {
        this.profileLookup = profileLookup;
        this.fuelPrices = fuelPrices;
        this.driverFuelReports = driverFuelReports;
    }

    @Transactional(readOnly = true)
    FuelPriceResponse latest(String fuelType, String state, String city) {
        return FuelPriceResponse.from(requireLatestFuelPrice(fuelType, state, city));
    }

    @Transactional(readOnly = true)
    List<FuelPriceResponse> history(String fuelType, String state, String city) {
        String normalizedFuel = normalizeFuelType(fuelType);
        String normalizedState = normalizeState(state);
        String normalizedCity = normalizeNullable(city);
        List<FuelPrice> prices = normalizedCity == null
            ? fuelPrices.findByFuelTypeAndStateOrderBySourcePeriodStartAsc(normalizedFuel, normalizedState)
            : fuelPrices.findByFuelTypeAndStateAndCityOrderBySourcePeriodStartAsc(
                normalizedFuel,
                normalizedState,
                normalizedCity
            );
        return prices.stream().map(FuelPriceResponse::from).toList();
    }

    @Transactional
    DriverFuelReportResponse createDriverReport(AppUser user, DriverFuelReportRequest request) {
        ProfileDriverOwner owner = profileLookup.requireDriverOwner(user);
        if (!"BRL".equals(request.currency())) {
            throw new IllegalArgumentException("currency must be BRL.");
        }
        var report = driverFuelReports.save(new DriverFuelReport(user.getAccountId(), owner.driverId(), request));
        return DriverFuelReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    FuelEstimateResponse estimate(
        String tripId,
        String truckId,
        String originState,
        String originCity,
        BigDecimal persistedDistanceKm,
        ProfileTruckConsumption consumption,
        FuelEstimateRequest request
    ) {
        BigDecimal routeKm = request.routeKm() == null ? persistedDistanceKm : request.routeKm();
        String loadStatus = request.loadStatus() == null || request.loadStatus().isBlank()
            ? "LOADED"
            : request.loadStatus().trim().toUpperCase();
        BigDecimal consumptionKmPerLiter = request.consumptionKmPerLiter() == null
            ? consumptionFor(loadStatus, consumption)
            : request.consumptionKmPerLiter();
        FuelPrice price = requireLatestFuelPrice(consumption.fuelType(), originState, originCity);
        BigDecimal dieselPrice = request.pricePerLiterOverride() == null
            ? price.getPricePerLiter()
            : request.pricePerLiterOverride();
        BigDecimal estimatedLiters = routeKm.divide(consumptionKmPerLiter, 2, RoundingMode.HALF_UP);
        BigDecimal estimatedFuelCost = estimatedLiters.multiply(dieselPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal safetyMargin = request.safetyMarginPercent() == null ? DEFAULT_SAFETY_MARGIN : request.safetyMarginPercent();
        BigDecimal recommendedBudget = estimatedFuelCost
            .multiply(BigDecimal.ONE.add(safetyMargin.divide(new BigDecimal("100.00"), 6, RoundingMode.HALF_UP)))
            .setScale(2, RoundingMode.HALF_UP);

        return new FuelEstimateResponse(
            tripId,
            decimal(routeKm, 2),
            truckId,
            loadStatus,
            decimal(consumptionKmPerLiter, 4),
            decimal(dieselPrice, 4),
            decimal(estimatedLiters, 2),
            decimal(estimatedFuelCost, 2),
            decimal(safetyMargin, 2),
            decimal(recommendedBudget, 2),
            "BRL",
            request.pricePerLiterOverride() == null ? price.getSource() : "driver_override",
            request.pricePerLiterOverride() == null ? price.getSourceType() : "driver_entered_override",
            request.pricePerLiterOverride() == null ? price.getConfidence() : "driver_confirmed",
            request.pricePerLiterOverride() == null ? price.getFreshnessStatus() : "CURRENT",
            request.pricePerLiterOverride() == null ? price.getSourcePeriodStart() : null,
            request.pricePerLiterOverride() == null ? price.getSourcePeriodEnd() : null,
            request.pricePerLiterOverride() == null ? price.getImportAuditId() : null,
            "calc_fuel_" + UUID.randomUUID().toString().replace("-", "")
        );
    }

    private FuelPrice requireLatestFuelPrice(String fuelType, String state, String city) {
        String normalizedFuel = normalizeFuelType(fuelType);
        String normalizedState = normalizeState(state);
        String normalizedCity = normalizeNullable(city);
        return (normalizedCity == null
            ? fuelPrices.findFirstByFuelTypeAndStateOrderBySourcePeriodEndDescRetrievedAtDesc(normalizedFuel, normalizedState)
            : fuelPrices.findFirstByFuelTypeAndStateAndCityOrderBySourcePeriodEndDescRetrievedAtDesc(
                normalizedFuel,
                normalizedState,
                normalizedCity
            ).or(() -> fuelPrices.findFirstByFuelTypeAndStateOrderBySourcePeriodEndDescRetrievedAtDesc(
                normalizedFuel,
                normalizedState
            )))
            .orElseThrow(() -> new ImportDataNotFoundException("Fuel price not found."));
    }

    private static BigDecimal consumptionFor(String loadStatus, ProfileTruckConsumption consumption) {
        if ("EMPTY".equals(loadStatus)) {
            return consumption.emptyAvgKmPerLiter();
        }
        return consumption.loadedAvgKmPerLiter();
    }

    private static String normalizeFuelType(String fuelType) {
        return fuelType == null || fuelType.isBlank() ? DEFAULT_FUEL_TYPE : fuelType.trim().toUpperCase();
    }

    private static String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("state is required.");
        }
        return state.trim().toUpperCase();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private static String decimal(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }
}
