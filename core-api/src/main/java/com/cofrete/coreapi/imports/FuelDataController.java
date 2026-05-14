package com.cofrete.coreapi.imports;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import com.cofrete.coreapi.profile.ProfileLookupService;
import com.cofrete.coreapi.trip.TripFuelLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FuelDataController {

    private final AuthenticatedUserService users;
    private final ProfileLookupService profileLookup;
    private final TripFuelLookupService tripLookup;
    private final FuelDataService fuelData;

    FuelDataController(
        AuthenticatedUserService users,
        ProfileLookupService profileLookup,
        TripFuelLookupService tripLookup,
        FuelDataService fuelData
    ) {
        this.users = users;
        this.profileLookup = profileLookup;
        this.tripLookup = tripLookup;
        this.fuelData = fuelData;
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/fuel-prices/latest")
    FuelPriceEnvelope latest(
        Authentication authentication,
        @RequestParam String fuel,
        @RequestParam String state,
        @RequestParam(required = false) String city
    ) {
        users.requireUser(authentication);
        return new FuelPriceEnvelope(fuelData.latest(fuel, state, city));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/fuel-prices/history")
    FuelPricesEnvelope history(
        Authentication authentication,
        @RequestParam String fuel,
        @RequestParam String state,
        @RequestParam(required = false) String city
    ) {
        users.requireUser(authentication);
        return new FuelPricesEnvelope(fuelData.history(fuel, state, city));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/fuel-prices/driver-report")
    @ResponseStatus(HttpStatus.CREATED)
    DriverFuelReportEnvelope driverReport(
        Authentication authentication,
        @Valid @RequestBody DriverFuelReportRequest request
    ) {
        return new DriverFuelReportEnvelope(fuelData.createDriverReport(users.requireUser(authentication), request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/fuel-estimate")
    FuelEstimateEnvelope fuelEstimate(
        Authentication authentication,
        @PathVariable String tripId,
        @Valid @RequestBody FuelEstimateRequest request
    ) {
        var user = users.requireUser(authentication);
        var context = tripLookup.requireFuelEstimateContext(user, tripId);
        var consumption = profileLookup.requireTruckConsumption(user, context.truckId());
        return new FuelEstimateEnvelope(fuelData.estimate(
            context.tripId(),
            context.truckId(),
            context.originState(),
            context.originCity(),
            context.totalDistanceKm(),
            consumption,
            request
        ));
    }
}
