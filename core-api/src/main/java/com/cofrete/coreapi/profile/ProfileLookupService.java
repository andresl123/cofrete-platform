package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileLookupService {

    private final DriverRepository drivers;
    private final TruckRepository trucks;
    private final TruckConsumptionProfileRepository consumptionProfiles;

    ProfileLookupService(
        DriverRepository drivers,
        TruckRepository trucks,
        TruckConsumptionProfileRepository consumptionProfiles
    ) {
        this.drivers = drivers;
        this.trucks = trucks;
        this.consumptionProfiles = consumptionProfiles;
    }

    @Transactional(readOnly = true)
    public ProfileDriverOwner requireDriverOwner(AppUser user) {
        var driver = drivers.findByUser(user)
            .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found."));
        return new ProfileDriverOwner(driver.getId(), user.getAccountId());
    }

    @Transactional(readOnly = true)
    public ProfileTripOwner requireTripOwner(AppUser user, String truckId) {
        var driver = drivers.findByUser(user)
            .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found."));
        var truck = trucks.findById(truckId)
            .orElseThrow(() -> new ProfileNotFoundException("Truck not found."));
        if (!truck.getDriver().getId().equals(driver.getId())) {
            throw new ProfileForbiddenException("Truck is not visible to this driver.");
        }
        return new ProfileTripOwner(driver.getId(), truck.getId());
    }

    @Transactional(readOnly = true)
    public ProfileTruckConsumption requireTruckConsumption(AppUser user, String truckId) {
        var driver = drivers.findByUser(user)
            .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found."));
        var truck = trucks.findById(truckId)
            .orElseThrow(() -> new ProfileNotFoundException("Truck not found."));
        if (!truck.getDriver().getId().equals(driver.getId())) {
            throw new ProfileForbiddenException("Truck is not visible to this driver.");
        }
        var profile = consumptionProfiles.findById(truck.getId());
        return profile.map(consumption -> new ProfileTruckConsumption(
                driver.getId(),
                truck.getId(),
                truck.getFuelType().name(),
                consumption.getLoadedAvgKmL(),
                consumption.getEmptyAvgKmL(),
                consumption.getLast30DaysAvgKmL(),
                consumption.getConfidence()
            ))
            .orElseGet(() -> new ProfileTruckConsumption(
                driver.getId(),
                truck.getId(),
                truck.getFuelType().name(),
                ProfileService.defaultLoadedConsumption(truck),
                ProfileService.defaultEmptyConsumption(truck),
                ProfileService.defaultLoadedConsumption(truck),
                "default_by_fuel_type"
            ));
    }
}
