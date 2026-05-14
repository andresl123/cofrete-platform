package com.cofrete.coreapi.receivables;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReceivableController {

    private final AuthenticatedUserService users;
    private final ReceivableService receivables;

    ReceivableController(AuthenticatedUserService users, ReceivableService receivables) {
        this.users = users;
        this.receivables = receivables;
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/customers")
    @ResponseStatus(HttpStatus.CREATED)
    CustomerEnvelope createCustomer(Authentication authentication, @Valid @RequestBody CustomerRequest request) {
        return new CustomerEnvelope(receivables.createCustomer(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/customers/{customerId}/profitability")
    CustomerProfitabilityEnvelope customerProfitability(
        Authentication authentication,
        @PathVariable String customerId
    ) {
        return new CustomerProfitabilityEnvelope(
            receivables.customerProfitability(users.requireUser(authentication), customerId)
        );
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/receivables")
    @ResponseStatus(HttpStatus.CREATED)
    ReceivableEnvelope createReceivable(Authentication authentication, @Valid @RequestBody ReceivableRequest request) {
        return new ReceivableEnvelope(receivables.createReceivable(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/receivables")
    ReceivablesEnvelope listReceivables(
        Authentication authentication,
        @RequestParam(required = false) String status
    ) {
        return receivables.listReceivables(users.requireUser(authentication), status);
    }

    @PutMapping(ApiRoutingConventions.API_PREFIX + "/receivables/{receivableId}/mark-paid")
    ReceivableEnvelope markPaid(
        Authentication authentication,
        @PathVariable String receivableId,
        @Valid @RequestBody MarkReceivablePaidRequest request
    ) {
        return new ReceivableEnvelope(receivables.markPaid(users.requireUser(authentication), receivableId, request));
    }
}
