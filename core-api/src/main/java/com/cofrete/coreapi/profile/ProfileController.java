package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProfileController {

    private final AuthenticatedUserService users;
    private final ProfileService profiles;

    ProfileController(AuthenticatedUserService users, ProfileService profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    DriverEnvelope createDriver(Authentication authentication, @Valid @RequestBody CreateDriverRequest request) {
        return new DriverEnvelope(profiles.createDriver(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/drivers/me")
    DriverEnvelope getDriver(Authentication authentication) {
        return new DriverEnvelope(profiles.getCurrentDriver(users.requireUser(authentication)));
    }

    @PatchMapping(ApiRoutingConventions.API_PREFIX + "/drivers/me")
    DriverEnvelope patchDriver(Authentication authentication, @Valid @RequestBody PatchDriverRequest request) {
        return new DriverEnvelope(profiles.patchCurrentDriver(users.requireUser(authentication), request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trucks")
    @ResponseStatus(HttpStatus.CREATED)
    TruckEnvelope createTruck(Authentication authentication, @Valid @RequestBody TruckRequest request) {
        return new TruckEnvelope(profiles.createTruck(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trucks")
    TrucksEnvelope listTrucks(Authentication authentication, @RequestParam(required = false) Boolean active) {
        return new TrucksEnvelope(profiles.listTrucks(users.requireUser(authentication), active));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trucks/{truckId}")
    TruckEnvelope getTruck(Authentication authentication, @PathVariable String truckId) {
        return new TruckEnvelope(profiles.getTruck(users.requireUser(authentication), truckId));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trucks/{truckId}/consumption-profile")
    TruckConsumptionProfileEnvelope getTruckConsumptionProfile(
        Authentication authentication,
        @PathVariable String truckId
    ) {
        return new TruckConsumptionProfileEnvelope(profiles.consumptionProfile(users.requireUser(authentication), truckId));
    }

    @PutMapping(ApiRoutingConventions.API_PREFIX + "/trucks/{truckId}")
    TruckEnvelope replaceTruck(
        Authentication authentication,
        @PathVariable String truckId,
        @Valid @RequestBody TruckRequest request
    ) {
        return new TruckEnvelope(profiles.replaceTruck(users.requireUser(authentication), truckId, request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/tax-profile")
    @ResponseStatus(HttpStatus.CREATED)
    TaxProfileEnvelope createTaxProfile(Authentication authentication, @Valid @RequestBody TaxProfileRequest request) {
        return new TaxProfileEnvelope(profiles.createOrReplaceTaxProfile(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/tax-profile")
    TaxProfileEnvelope getTaxProfile(Authentication authentication, @RequestParam(required = false) Integer year) {
        return new TaxProfileEnvelope(profiles.getTaxProfile(users.requireUser(authentication), year));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/source-rules/ipva")
    @ResponseStatus(HttpStatus.CREATED)
    IpvaRuleEnvelope upsertIpvaRule(Authentication authentication, @Valid @RequestBody IpvaRuleRequest request) {
        users.requireUser(authentication);
        return new IpvaRuleEnvelope(profiles.createOrUpdateIpvaRule(request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/source-rules/ipva")
    IpvaRuleEnvelope getIpvaRule(
        Authentication authentication,
        @RequestParam String state,
        @RequestParam VehicleType vehicleType,
        @RequestParam int effectiveYear
    ) {
        users.requireUser(authentication);
        return new IpvaRuleEnvelope(profiles.getIpvaRule(state, vehicleType, effectiveYear));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/source-rules/tax-years")
    @ResponseStatus(HttpStatus.CREATED)
    TaxRuleYearEnvelope upsertTaxRuleYear(Authentication authentication, @Valid @RequestBody TaxRuleYearRequest request) {
        users.requireUser(authentication);
        return new TaxRuleYearEnvelope(profiles.createOrUpdateTaxRuleYear(request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/source-rules/tax-years")
    TaxRuleYearEnvelope getTaxRuleYear(
        Authentication authentication,
        @RequestParam TaxRegime regime,
        @RequestParam int planningYear
    ) {
        users.requireUser(authentication);
        return new TaxRuleYearEnvelope(profiles.getTaxRuleYear(regime, planningYear));
    }
}
