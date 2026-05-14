package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProfileService {

    private final DriverRepository drivers;
    private final TruckRepository trucks;
    private final TruckConsumptionProfileRepository consumptionProfiles;
    private final TaxProfileRepository taxProfiles;
    private final IpvaRuleRepository ipvaRules;
    private final TaxRuleYearRepository taxRuleYears;

    ProfileService(
        DriverRepository drivers,
        TruckRepository trucks,
        TruckConsumptionProfileRepository consumptionProfiles,
        TaxProfileRepository taxProfiles,
        IpvaRuleRepository ipvaRules,
        TaxRuleYearRepository taxRuleYears
    ) {
        this.drivers = drivers;
        this.trucks = trucks;
        this.consumptionProfiles = consumptionProfiles;
        this.taxProfiles = taxProfiles;
        this.ipvaRules = ipvaRules;
        this.taxRuleYears = taxRuleYears;
    }

    @Transactional
    DriverResponse createDriver(AppUser user, CreateDriverRequest request) {
        if (drivers.existsByUser(user)) {
            throw new ProfileConflictException("Driver profile already exists for this principal.");
        }

        var driver = drivers.save(new Driver(user, request));
        return DriverResponse.from(driver);
    }

    @Transactional(readOnly = true)
    Driver requireDriver(AppUser user) {
        return drivers.findByUser(user)
            .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found."));
    }

    @Transactional(readOnly = true)
    DriverResponse getCurrentDriver(AppUser user) {
        return DriverResponse.from(requireDriver(user));
    }

    @Transactional
    DriverResponse patchCurrentDriver(AppUser user, PatchDriverRequest request) {
        var driver = requireDriver(user);
        driver.patch(request);
        return DriverResponse.from(driver);
    }

    @Transactional
    TruckResponse createTruck(AppUser user, TruckRequest request) {
        var driver = requireDriver(user);
        var plate = request.plate().trim().toUpperCase();
        if (trucks.existsByDriverAndPlate(driver, plate)) {
            throw new ProfileConflictException("Truck plate already exists for this driver.");
        }

        return TruckResponse.from(trucks.save(new Truck(driver, request)));
    }

    @Transactional(readOnly = true)
    List<TruckResponse> listTrucks(AppUser user, Boolean active) {
        var driver = requireDriver(user);
        var results = active == null
            ? trucks.findByDriverOrderByCreatedAtAsc(driver)
            : trucks.findByDriverAndActiveOrderByCreatedAtAsc(driver, active);
        return results.stream().map(TruckResponse::from).toList();
    }

    @Transactional(readOnly = true)
    TruckResponse getTruck(AppUser user, String truckId) {
        return TruckResponse.from(requireOwnedTruck(user, truckId));
    }

    @Transactional
    TruckResponse replaceTruck(AppUser user, String truckId, TruckRequest request) {
        var truck = requireOwnedTruck(user, truckId);
        truck.updateFrom(request);
        return TruckResponse.from(truck);
    }

    @Transactional(readOnly = true)
    TruckConsumptionProfileResponse consumptionProfile(AppUser user, String truckId) {
        var truck = requireOwnedTruck(user, truckId);
        return consumptionProfiles.findById(truck.getId())
            .map(TruckConsumptionProfileResponse::from)
            .orElseGet(() -> TruckConsumptionProfileResponse.defaultFor(truck));
    }

    @Transactional
    TaxProfileResponse createOrReplaceTaxProfile(AppUser user, TaxProfileRequest request) {
        var driver = requireDriver(user);
        var profile = taxProfiles.findByDriverAndPlanningYear(driver, request.planningYear())
            .orElseGet(() -> new TaxProfile(driver, request));
        profile.updateFrom(request);
        return TaxProfileResponse.from(taxProfiles.save(profile));
    }

    @Transactional(readOnly = true)
    TaxProfileResponse getTaxProfile(AppUser user, Integer year) {
        var driver = requireDriver(user);
        var profile = year == null
            ? taxProfiles.findFirstByDriverOrderByPlanningYearDesc(driver)
            : taxProfiles.findByDriverAndPlanningYear(driver, year);

        return TaxProfileResponse.from(profile.orElseThrow(() -> new ProfileNotFoundException("Tax profile not found.")));
    }

    @Transactional
    IpvaRuleResponse createOrUpdateIpvaRule(IpvaRuleRequest request) {
        var state = request.state().trim().toUpperCase();
        var rule = ipvaRules.findByStateAndVehicleTypeAndEffectiveYear(
                state,
                request.vehicleType(),
                request.effectiveYear()
            )
            .orElseGet(() -> new IpvaRule(request));
        rule.updateFrom(request);
        return IpvaRuleResponse.from(ipvaRules.save(rule));
    }

    @Transactional(readOnly = true)
    IpvaRuleResponse getIpvaRule(String state, VehicleType vehicleType, int effectiveYear) {
        var normalizedState = state.trim().toUpperCase();
        return ipvaRules.findByStateAndVehicleTypeAndEffectiveYear(normalizedState, vehicleType, effectiveYear)
            .map(IpvaRuleResponse::from)
            .orElseGet(() -> IpvaRuleResponse.missing(normalizedState, vehicleType, effectiveYear));
    }

    @Transactional
    TaxRuleYearResponse createOrUpdateTaxRuleYear(TaxRuleYearRequest request) {
        var rule = taxRuleYears.findByRegimeAndPlanningYear(request.regime(), request.planningYear())
            .orElseGet(() -> new TaxRuleYear(request));
        rule.updateFrom(request);
        return TaxRuleYearResponse.from(taxRuleYears.save(rule));
    }

    @Transactional(readOnly = true)
    TaxRuleYearResponse getTaxRuleYear(TaxRegime regime, int planningYear) {
        return taxRuleYears.findByRegimeAndPlanningYear(regime, planningYear)
            .map(TaxRuleYearResponse::from)
            .orElseGet(() -> TaxRuleYearResponse.missing(regime, planningYear));
    }

    private Truck requireOwnedTruck(AppUser user, String truckId) {
        var driver = requireDriver(user);
        var truck = trucks.findById(truckId)
            .orElseThrow(() -> new ProfileNotFoundException("Truck not found."));
        if (!truck.getDriver().getId().equals(driver.getId())) {
            throw new ProfileForbiddenException("Truck is not visible to this driver.");
        }
        return truck;
    }

    int currentPlanningYear() {
        return Year.now().getValue();
    }

    static BigDecimal defaultLoadedConsumption(Truck truck) {
        return switch (truck.getFuelType()) {
            case DIESEL_S10, DIESEL_S500 -> new BigDecimal("2.3500");
            case BIODIESEL -> new BigDecimal("2.2000");
            case OTHER -> new BigDecimal("2.0000");
        };
    }

    static BigDecimal defaultEmptyConsumption(Truck truck) {
        return switch (truck.getFuelType()) {
            case DIESEL_S10, DIESEL_S500 -> new BigDecimal("3.1000");
            case BIODIESEL -> new BigDecimal("2.9000");
            case OTHER -> new BigDecimal("2.5000");
        };
    }
}
