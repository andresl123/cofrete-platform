package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReserveController {

    private final AuthenticatedUserService users;
    private final ReserveService reserves;

    ReserveController(AuthenticatedUserService users, ReserveService reserves) {
        this.users = users;
        this.reserves = reserves;
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/reserve-rules")
    @ResponseStatus(HttpStatus.CREATED)
    ReserveRuleEnvelope createReserveRule(
        Authentication authentication,
        @Valid @RequestBody ReserveRuleRequest request
    ) {
        return new ReserveRuleEnvelope(reserves.createOrUpdateRule(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/reserve-wallets")
    ReserveWalletsEnvelope listReserveWallets(Authentication authentication) {
        return new ReserveWalletsEnvelope(reserves.listWallets(users.requireUser(authentication)));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/reserve-allocations")
    @ResponseStatus(HttpStatus.CREATED)
    ReserveAllocationEnvelope createReserveAllocation(
        Authentication authentication,
        @Valid @RequestBody ReserveAllocationRequest request
    ) {
        return new ReserveAllocationEnvelope(reserves.allocate(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/financial-health-score")
    FinancialHealthEnvelope getFinancialHealth(Authentication authentication) {
        return new FinancialHealthEnvelope(reserves.financialHealth(users.requireUser(authentication)));
    }
}
