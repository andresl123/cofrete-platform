package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileLookupService {

    private final DriverRepository drivers;
    private final TruckRepository trucks;

    ProfileLookupService(DriverRepository drivers, TruckRepository trucks) {
        this.drivers = drivers;
        this.trucks = trucks;
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
}
