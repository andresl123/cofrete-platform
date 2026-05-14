package com.cofrete.coreapi.trip;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import jakarta.servlet.http.HttpServletRequest;
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
class TripController {

    private final AuthenticatedUserService users;
    private final TripService trips;

    TripController(AuthenticatedUserService users, TripService trips) {
        this.users = users;
        this.trips = trips;
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips")
    @ResponseStatus(HttpStatus.CREATED)
    TripEnvelope createTrip(
        Authentication authentication,
        @Valid @RequestBody CreateTripRequest request,
        HttpServletRequest servletRequest
    ) {
        return new TripEnvelope(trips.createTrip(users.requireUser(authentication), request, correlationId(servletRequest)));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}")
    TripEnvelope getTrip(Authentication authentication, @PathVariable String tripId) {
        return new TripEnvelope(trips.getTrip(users.requireUser(authentication), tripId));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/profitability-estimate")
    ProfitabilityEstimateEnvelope estimate(
        Authentication authentication,
        @PathVariable String tripId,
        @Valid @RequestBody ProfitabilityEstimateRequest request,
        HttpServletRequest servletRequest
    ) {
        return new ProfitabilityEstimateEnvelope(
            trips.estimate(users.requireUser(authentication), tripId, request, correlationId(servletRequest))
        );
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/profitability-snapshot")
    ProfitabilitySnapshotEnvelope snapshot(Authentication authentication, @PathVariable String tripId) {
        return new ProfitabilitySnapshotEnvelope(trips.latestSnapshot(users.requireUser(authentication), tripId));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/acceptance-decision")
    AcceptanceDecisionEnvelope decide(
        Authentication authentication,
        @PathVariable String tripId,
        @Valid @RequestBody AcceptanceDecisionRequest request
    ) {
        return new AcceptanceDecisionEnvelope(trips.decide(users.requireUser(authentication), tripId, request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/toll-estimates")
    TollEstimateEnvelope estimateTolls(
        Authentication authentication,
        @Valid @RequestBody TollEstimateRequest request
    ) {
        return new TollEstimateEnvelope(trips.estimateTolls(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/tolls")
    TripTollsEnvelope getTolls(Authentication authentication, @PathVariable String tripId) {
        return trips.getTolls(users.requireUser(authentication), tripId);
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/tolls/manual-payment")
    @ResponseStatus(HttpStatus.CREATED)
    TripTollEnvelope recordManualTollPayment(
        Authentication authentication,
        @PathVariable String tripId,
        @Valid @RequestBody ManualTollPaymentRequest request,
        HttpServletRequest servletRequest
    ) {
        return new TripTollEnvelope(
            trips.recordManualTollPayment(users.requireUser(authentication), tripId, request, correlationId(servletRequest))
        );
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/trips/{tripId}/vale-pedagio")
    @ResponseStatus(HttpStatus.CREATED)
    ValePedagioEnvelope recordValePedagio(
        Authentication authentication,
        @PathVariable String tripId,
        @Valid @RequestBody ValePedagioRequest request,
        HttpServletRequest servletRequest
    ) {
        return new ValePedagioEnvelope(
            trips.recordValePedagio(users.requireUser(authentication), tripId, request, correlationId(servletRequest))
        );
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/toll-data/import-status")
    TollDataImportStatusEnvelope tollDataImportStatus(
        Authentication authentication,
        @RequestParam(required = false) String source
    ) {
        users.requireUser(authentication);
        return new TollDataImportStatusEnvelope(trips.tollDataImportStatus(source));
    }

    private static String correlationId(HttpServletRequest request) {
        return request.getHeader("X-Correlation-Id");
    }
}
