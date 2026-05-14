package com.cofrete.coreapi.compliance;

import com.cofrete.coreapi.auth.AuthenticatedUserService;
import com.cofrete.coreapi.config.ApiRoutingConventions;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ComplianceController {

    private final AuthenticatedUserService users;
    private final ComplianceService compliance;

    ComplianceController(AuthenticatedUserService users, ComplianceService compliance) {
        this.users = users;
        this.compliance = compliance;
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/compliance/profile")
    ComplianceProfileEnvelope getComplianceProfile(Authentication authentication) {
        return new ComplianceProfileEnvelope(compliance.getProfile(users.requireUser(authentication)));
    }

    @PutMapping(ApiRoutingConventions.API_PREFIX + "/compliance/rntrc")
    RntrcProfileEnvelope updateRntrc(
        Authentication authentication,
        @Valid @RequestBody RntrcMetadataRequest request
    ) {
        return new RntrcProfileEnvelope(compliance.updateRntrc(users.requireUser(authentication), request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/compliance/rntrc/check-public-status")
    RntrcPublicStatusCheckEnvelope checkPublicStatus(
        Authentication authentication,
        @Valid @RequestBody RntrcPublicStatusCheckRequest request
    ) {
        return new RntrcPublicStatusCheckEnvelope(compliance.checkPublicStatus(users.requireUser(authentication), request));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/compliance/insurance-policies")
    @ResponseStatus(HttpStatus.CREATED)
    InsurancePolicyEnvelope createInsurancePolicy(
        Authentication authentication,
        @Valid @RequestBody InsurancePolicyRequest request
    ) {
        return new InsurancePolicyEnvelope(compliance.createInsurancePolicy(users.requireUser(authentication), request));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/compliance/insurance-policies")
    InsurancePoliciesEnvelope listInsurancePolicies(
        Authentication authentication,
        @RequestParam(required = false) Boolean active
    ) {
        return new InsurancePoliciesEnvelope(compliance.listInsurancePolicies(users.requireUser(authentication), active));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/compliance/score")
    ComplianceScoreEnvelope getScore(Authentication authentication) {
        return new ComplianceScoreEnvelope(compliance.getScore(users.requireUser(authentication)));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/compliance/calendar")
    ComplianceCalendarEnvelope getCalendar(
        Authentication authentication,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return new ComplianceCalendarEnvelope(compliance.getCalendar(users.requireUser(authentication), from, to));
    }

    @GetMapping(ApiRoutingConventions.API_PREFIX + "/documents")
    ComplianceDocumentsEnvelope listDocuments(
        Authentication authentication,
        @RequestParam(required = false) ComplianceDocumentType type,
        @RequestParam(required = false) Boolean active
    ) {
        return new ComplianceDocumentsEnvelope(compliance.listDocuments(users.requireUser(authentication), type, active));
    }

    @PostMapping(ApiRoutingConventions.API_PREFIX + "/documents")
    @ResponseStatus(HttpStatus.CREATED)
    ComplianceDocumentEnvelope createDocument(
        Authentication authentication,
        @Valid @RequestBody ComplianceDocumentRequest request
    ) {
        return new ComplianceDocumentEnvelope(compliance.createDocument(users.requireUser(authentication), request));
    }
}
