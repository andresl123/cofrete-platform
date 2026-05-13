package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProfileService {

    private final DriverRepository drivers;
    private final TruckRepository trucks;
    private final TaxProfileRepository taxProfiles;

    ProfileService(DriverRepository drivers, TruckRepository trucks, TaxProfileRepository taxProfiles) {
        this.drivers = drivers;
        this.trucks = trucks;
        this.taxProfiles = taxProfiles;
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
}
