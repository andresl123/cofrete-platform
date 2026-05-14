package com.cofrete.coreapi.compliance;

import com.cofrete.coreapi.auth.AppUser;
import com.cofrete.coreapi.profile.ProfileLookupService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ComplianceService {

    private final ProfileLookupService profiles;
    private final ComplianceProfileRepository complianceProfiles;
    private final InsurancePolicyRepository insurancePolicies;
    private final ComplianceDocumentRepository documents;
    private final ComplianceAlertRepository alerts;
    private final ComplianceScoreSnapshotRepository scoreSnapshots;
    private final ComplianceCalendarItemRepository calendarItems;
    private final WaitingTimeRuleRepository waitingTimeRules;
    private final InsuranceRequirementRuleRepository insuranceRequirementRules;
    private final Clock clock;

    ComplianceService(
        ProfileLookupService profiles,
        ComplianceProfileRepository complianceProfiles,
        InsurancePolicyRepository insurancePolicies,
        ComplianceDocumentRepository documents,
        ComplianceAlertRepository alerts,
        ComplianceScoreSnapshotRepository scoreSnapshots,
        ComplianceCalendarItemRepository calendarItems,
        WaitingTimeRuleRepository waitingTimeRules,
        InsuranceRequirementRuleRepository insuranceRequirementRules
    ) {
        this.profiles = profiles;
        this.complianceProfiles = complianceProfiles;
        this.insurancePolicies = insurancePolicies;
        this.documents = documents;
        this.alerts = alerts;
        this.scoreSnapshots = scoreSnapshots;
        this.calendarItems = calendarItems;
        this.waitingTimeRules = waitingTimeRules;
        this.insuranceRequirementRules = insuranceRequirementRules;
        clock = Clock.systemUTC();
    }

    @Transactional
    ComplianceProfileResponse getProfile(AppUser user) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        var profile = requireOrCreateProfile(driverId);
        refreshDerivedItems(driverId);
        var activePolicies = insurancePolicies.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true);
        var activeDocuments = documents.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true);
        var score = calculateScore(profile, activePolicies, activeDocuments);
        return new ComplianceProfileResponse(
            driverId,
            RntrcProfileResponse.from(profile),
            new CiotResponse(
                profile.getCiotRequired(),
                profile.getLatestCiotStatus(),
                "CIOT guidance is advisory until confirmed with official or professional sources."
            ),
            insuranceSummary(activePolicies, insuranceSourceReview()),
            documentSummary(activeDocuments),
            score,
            alerts.findByDriverIdOrderByDueOnAscGeneratedAtAsc(driverId).stream().map(ComplianceAlertResponse::from).toList(),
            ComplianceWording.caveats()
        );
    }

    @Transactional
    RntrcProfileResponse updateRntrc(AppUser user, RntrcMetadataRequest request) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        var profile = requireOrCreateProfile(driverId);
        profile.updateFrom(request);
        var saved = complianceProfiles.save(profile);
        refreshDerivedItems(driverId);
        return RntrcProfileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    RntrcPublicStatusCheckResponse checkPublicStatus(AppUser user, RntrcPublicStatusCheckRequest request) {
        profiles.requireDriverOwner(user);
        return new RntrcPublicStatusCheckResponse(
            "UNSUPPORTED",
            "ANTT public consultation",
            ComplianceWording.RNTRC_PUBLIC_CONSULTATION_URL,
            ComplianceWording.RNTRC_PUBLIC_CHECK_UNSUPPORTED,
            ComplianceWording.rntrcGuidance()
        );
    }

    @Transactional
    InsurancePolicyResponse createInsurancePolicy(AppUser user, InsurancePolicyRequest request) {
        validateInsuranceDates(request);
        var driverId = profiles.requireDriverOwner(user).driverId();
        var truckId = request.truckId() == null || request.truckId().isBlank()
            ? null
            : profiles.requireTripOwner(user, request.truckId()).truckId();
        var policy = insurancePolicies.save(new InsurancePolicy(driverId, truckId, request));
        refreshDerivedItems(driverId);
        return InsurancePolicyResponse.from(policy, insuranceSourceReview(policy.getPolicyType()));
    }

    @Transactional(readOnly = true)
    List<InsurancePolicyResponse> listInsurancePolicies(AppUser user, Boolean active) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        var results = active == null
            ? insurancePolicies.findByDriverIdOrderByCreatedAtAsc(driverId)
            : insurancePolicies.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, active);
        return results.stream()
            .map(policy -> InsurancePolicyResponse.from(policy, insuranceSourceReview(policy.getPolicyType())))
            .toList();
    }

    @Transactional
    ComplianceScoreResponse getScore(AppUser user) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        var profile = requireOrCreateProfile(driverId);
        refreshDerivedItems(driverId);
        var score = calculateScore(
            profile,
            insurancePolicies.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true),
            documents.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true)
        );
        scoreSnapshots.save(new ComplianceScoreSnapshot(driverId, score));
        return score;
    }

    @Transactional
    List<ComplianceCalendarItemResponse> getCalendar(AppUser user, LocalDate from, LocalDate to) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        if (from != null && to != null && from.isAfter(to)) {
            throw new ComplianceValidationException("Calendar from date must be on or before to date.");
        }
        refreshDerivedItems(driverId);
        var results = from == null || to == null
            ? calendarItems.findByDriverIdOrderByDueOnAsc(driverId)
            : calendarItems.findByDriverIdAndDueOnBetweenOrderByDueOnAsc(driverId, from, to);
        return results.stream().map(ComplianceCalendarItemResponse::from).toList();
    }

    @Transactional
    ComplianceDocumentResponse createDocument(AppUser user, ComplianceDocumentRequest request) {
        validateDocumentDates(request);
        var driverId = profiles.requireDriverOwner(user).driverId();
        var ownerId = normalizeDocumentOwner(user, request);
        var document = documents.save(new ComplianceDocument(driverId, ownerId, request));
        refreshDerivedItems(driverId);
        return ComplianceDocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    List<ComplianceDocumentResponse> listDocuments(
        AppUser user,
        ComplianceDocumentType documentType,
        Boolean active
    ) {
        var driverId = profiles.requireDriverOwner(user).driverId();
        var results = documentType == null && active == null
            ? documents.findByDriverIdOrderByCreatedAtAsc(driverId)
            : documentType == null
                ? documents.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, active)
                : active == null
                    ? documents.findByDriverIdAndDocumentTypeOrderByCreatedAtAsc(driverId, documentType)
                    : documents.findByDriverIdAndDocumentTypeAndActiveOrderByCreatedAtAsc(driverId, documentType, active);
        results.forEach(document -> document.refreshStatus(clock));
        return results.stream().map(ComplianceDocumentResponse::from).toList();
    }

    @Transactional
    WaitingTimeRuleResponse createOrUpdateWaitingTimeRule(WaitingTimeRuleRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new ComplianceValidationException("effectiveTo cannot be before effectiveFrom.");
        }
        var rule = waitingTimeRules.findByEffectiveFrom(request.effectiveFrom())
            .orElseGet(() -> new WaitingTimeRule(request));
        rule.updateFrom(request);
        return WaitingTimeRuleResponse.from(waitingTimeRules.save(rule));
    }

    @Transactional(readOnly = true)
    WaitingTimeRuleResponse getWaitingTimeRule(LocalDate effectiveDate) {
        var date = effectiveDate == null ? LocalDate.now(clock) : effectiveDate;
        return waitingTimeRules.findEffectiveRules(date, PageRequest.of(0, 1)).stream()
            .findFirst()
            .map(WaitingTimeRuleResponse::from)
            .orElseGet(() -> WaitingTimeRuleResponse.missing(date));
    }

    @Transactional
    InsuranceRequirementRuleResponse createOrUpdateInsuranceRequirementRule(InsuranceRequirementRuleRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new ComplianceValidationException("effectiveTo cannot be before effectiveFrom.");
        }
        var rule = insuranceRequirementRules
            .findByRequirementScopeAndPolicyTypeAndEffectiveFrom(
                request.requirementScope().trim(),
                request.policyType(),
                request.effectiveFrom()
            )
            .orElseGet(() -> new InsuranceRequirementRule(request));
        rule.updateFrom(request);
        return InsuranceRequirementRuleResponse.from(insuranceRequirementRules.save(rule));
    }

    @Transactional(readOnly = true)
    InsuranceRequirementRuleResponse getInsuranceRequirementRule(LocalDate effectiveDate) {
        var date = effectiveDate == null ? LocalDate.now(clock) : effectiveDate;
        return insuranceRequirementRules.findEffectiveRules(date, PageRequest.of(0, 1)).stream()
            .findFirst()
            .map(InsuranceRequirementRuleResponse::from)
            .orElseGet(() -> InsuranceRequirementRuleResponse.missing(date));
    }

    private ComplianceProfile requireOrCreateProfile(String driverId) {
        return complianceProfiles.findByDriverId(driverId)
            .orElseGet(() -> complianceProfiles.save(new ComplianceProfile(driverId)));
    }

    private String normalizeDocumentOwner(AppUser user, ComplianceDocumentRequest request) {
        if (request.ownerType() == ComplianceOwnerType.TRUCK) {
            if (request.ownerId() == null || request.ownerId().isBlank()) {
                throw new ComplianceValidationException("Truck-owned documents require ownerId.");
            }
            return profiles.requireTripOwner(user, request.ownerId()).truckId();
        }
        return request.ownerId() == null || request.ownerId().isBlank() ? null : request.ownerId();
    }

    private void validateInsuranceDates(InsurancePolicyRequest request) {
        if (request.startsOn() != null && request.expiresOn() != null && request.startsOn().isAfter(request.expiresOn())) {
            throw new ComplianceValidationException("Insurance startsOn must be on or before expiresOn.");
        }
    }

    private void validateDocumentDates(ComplianceDocumentRequest request) {
        if (request.issuedOn() != null && request.expiresOn() != null && request.issuedOn().isAfter(request.expiresOn())) {
            throw new ComplianceValidationException("Document issuedOn must be on or before expiresOn.");
        }
    }

    private InsuranceSummaryResponse insuranceSummary(List<InsurancePolicy> policies, InsuranceSourceReviewResponse sourceReview) {
        var statuses = policies.stream().map(policy -> ComplianceDates.statusFor(policy.getExpiresOn(), clock)).toList();
        return new InsuranceSummaryResponse(
            policies.size(),
            (int) statuses.stream().filter(ComplianceItemStatus.EXPIRING_SOON::equals).count(),
            (int) statuses.stream().filter(ComplianceItemStatus.EXPIRED::equals).count(),
            sourceReview.advisoryText(),
            sourceReview
        );
    }

    private DocumentSummaryResponse documentSummary(List<ComplianceDocument> activeDocuments) {
        return new DocumentSummaryResponse(
            activeDocuments.size(),
            (int) activeDocuments.stream()
                .filter(document -> ComplianceDates.statusFor(document.getExpiresOn(), clock) == ComplianceItemStatus.EXPIRING_SOON)
                .count(),
            (int) activeDocuments.stream()
                .filter(document -> ComplianceDates.statusFor(document.getExpiresOn(), clock) == ComplianceItemStatus.EXPIRED)
                .count(),
            ComplianceWording.DOCUMENT_CAVEAT
        );
    }

    private ComplianceScoreResponse calculateScore(
        ComplianceProfile profile,
        List<InsurancePolicy> policies,
        List<ComplianceDocument> activeDocuments
    ) {
        var components = new ArrayList<ComplianceScoreComponent>();
        var rntrcStatus = rntrcComponentStatus(profile);
        var insuranceStatus = insuranceComponentStatus(policies);
        var documentStatus = documentComponentStatus(activeDocuments);
        components.add(new ComplianceScoreComponent("rntrc", rntrcStatus, "35.00", ComplianceWording.RNTRC_NOT_OFFICIAL_RECORD));
        components.add(new ComplianceScoreComponent("insurance", insuranceStatus, "35.00", ComplianceWording.INSURANCE_CAVEAT));
        components.add(new ComplianceScoreComponent("documents", documentStatus, "30.00", ComplianceWording.DOCUMENT_CAVEAT));

        var score = 100;
        score -= deduction(rntrcStatus, 15, 35);
        score -= deduction(insuranceStatus, 15, 35);
        score -= deduction(documentStatus, 10, 25);
        var bounded = Math.max(0, score);
        return new ComplianceScoreResponse(
            new ComplianceScore(bounded, scoreStatus(bounded), Instant.now(clock), components),
            ComplianceWording.caveats()
        );
    }

    private ComplianceItemStatus rntrcComponentStatus(ComplianceProfile profile) {
        return switch (profile.getRntrcStatus()) {
            case ACTIVE -> ComplianceItemStatus.CURRENT;
            case INACTIVE, SUSPENDED, EXPIRED, CANCELLED -> ComplianceItemStatus.EXPIRED;
            case UNKNOWN -> ComplianceItemStatus.UNKNOWN;
        };
    }

    private ComplianceItemStatus insuranceComponentStatus(List<InsurancePolicy> policies) {
        if (policies.isEmpty()) {
            return ComplianceItemStatus.UNKNOWN;
        }
        var statuses = policies.stream().map(policy -> ComplianceDates.statusFor(policy.getExpiresOn(), clock)).toList();
        if (statuses.contains(ComplianceItemStatus.EXPIRED)) {
            return ComplianceItemStatus.EXPIRED;
        }
        if (statuses.contains(ComplianceItemStatus.EXPIRING_SOON) || statuses.contains(ComplianceItemStatus.UNKNOWN)) {
            return ComplianceItemStatus.EXPIRING_SOON;
        }
        return ComplianceItemStatus.CURRENT;
    }

    private ComplianceItemStatus documentComponentStatus(List<ComplianceDocument> activeDocuments) {
        if (activeDocuments.isEmpty()) {
            return ComplianceItemStatus.UNKNOWN;
        }
        if (activeDocuments.stream()
            .anyMatch(document -> ComplianceDates.statusFor(document.getExpiresOn(), clock) == ComplianceItemStatus.EXPIRED)) {
            return ComplianceItemStatus.EXPIRED;
        }
        if (activeDocuments.stream()
            .anyMatch(document -> ComplianceDates.statusFor(document.getExpiresOn(), clock) == ComplianceItemStatus.EXPIRING_SOON)) {
            return ComplianceItemStatus.EXPIRING_SOON;
        }
        return ComplianceItemStatus.CURRENT;
    }

    private int deduction(ComplianceItemStatus status, int warningPenalty, int riskPenalty) {
        return switch (status) {
            case CURRENT -> 0;
            case EXPIRING_SOON, UNKNOWN -> warningPenalty;
            case EXPIRED -> riskPenalty;
        };
    }

    private ComplianceScoreStatus scoreStatus(int score) {
        if (score >= 85) {
            return ComplianceScoreStatus.GOOD;
        }
        if (score >= 60) {
            return ComplianceScoreStatus.ATTENTION;
        }
        return ComplianceScoreStatus.RISK;
    }

    private void refreshDerivedItems(String driverId) {
        alerts.deleteByDriverId(driverId);
        calendarItems.deleteByDriverId(driverId);
        var generatedAlerts = new ArrayList<ComplianceAlert>();
        var generatedCalendar = new ArrayList<ComplianceCalendarItem>();
        complianceProfiles.findByDriverId(driverId).ifPresent(profile -> addRntrcAlert(driverId, profile, generatedAlerts));
        insurancePolicies.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true)
            .forEach(policy -> addInsuranceReminders(
                driverId,
                policy,
                generatedAlerts,
                generatedCalendar,
                insuranceSourceReview(policy.getPolicyType())
            ));
        documents.findByDriverIdAndActiveOrderByCreatedAtAsc(driverId, true)
            .forEach(document -> {
                document.refreshStatus(clock);
                addDocumentReminders(driverId, document, generatedAlerts, generatedCalendar);
            });
        alerts.saveAll(generatedAlerts);
        calendarItems.saveAll(generatedCalendar);
    }

    private void addRntrcAlert(String driverId, ComplianceProfile profile, List<ComplianceAlert> generatedAlerts) {
        var status = rntrcComponentStatus(profile);
        if (status == ComplianceItemStatus.CURRENT) {
            return;
        }
        generatedAlerts.add(new ComplianceAlert(
            driverId,
            ComplianceAlertType.RNTRC_STATUS,
            status == ComplianceItemStatus.EXPIRED ? ComplianceSeverity.CRITICAL : ComplianceSeverity.WARNING,
            status,
            ComplianceSubjectType.RNTRC,
            profile.getId(),
            null,
            "Confirm RNTRC status in ANTT/RNTRC Digital.",
            ComplianceWording.RNTRC_NOT_OFFICIAL_RECORD,
            ComplianceWording.RNTRC_OFFICIAL_ACTION_URL
        ));
    }

    private void addInsuranceReminders(
        String driverId,
        InsurancePolicy policy,
        List<ComplianceAlert> generatedAlerts,
        List<ComplianceCalendarItem> generatedCalendar,
        InsuranceSourceReviewResponse sourceReview
    ) {
        var status = ComplianceDates.statusFor(policy.getExpiresOn(), clock);
        if (status != ComplianceItemStatus.EXPIRED && status != ComplianceItemStatus.EXPIRING_SOON) {
            return;
        }
        var severity = status == ComplianceItemStatus.EXPIRED ? ComplianceSeverity.CRITICAL : ComplianceSeverity.WARNING;
        var message = "Insurance policy " + policy.getPolicyType() + " is " + status.name().toLowerCase().replace('_', ' ') + ".";
        var advisoryText = insuranceAlertAdvisoryText(sourceReview);
        generatedAlerts.add(new ComplianceAlert(
            driverId,
            ComplianceAlertType.INSURANCE_EXPIRATION,
            severity,
            status,
            ComplianceSubjectType.INSURANCE_POLICY,
            policy.getId(),
            policy.getExpiresOn(),
            message,
            advisoryText,
            ComplianceWording.INSURANCE_OFFICIAL_ACTION_URL
        ));
        generatedCalendar.add(new ComplianceCalendarItem(
            driverId,
            ComplianceSubjectType.INSURANCE_POLICY,
            policy.getId(),
            "Insurance policy " + policy.getPolicyType() + " renewal",
            policy.getExpiresOn(),
            status,
            severity,
            advisoryText,
            ComplianceWording.INSURANCE_OFFICIAL_ACTION_URL
        ));
    }

    private String insuranceAlertAdvisoryText(InsuranceSourceReviewResponse sourceReview) {
        return sourceReview.advisoryText()
            + " Source status: " + sourceReview.ruleStatus()
            + "; source: " + (sourceReview.sourceUrl() == null ? "not configured" : sourceReview.sourceUrl())
            + "; reviewedAt: " + (sourceReview.reviewedAt() == null ? "not reviewed" : sourceReview.reviewedAt())
            + "; confidence: " + sourceReview.confidence() + ".";
    }

    private InsuranceSourceReviewResponse insuranceSourceReview() {
        return insuranceRequirementRules.findEffectiveRules(LocalDate.now(clock), PageRequest.of(0, 1)).stream()
            .findFirst()
            .map(InsuranceSourceReviewResponse::from)
            .orElseGet(() -> InsuranceSourceReviewResponse.missing(LocalDate.now(clock)));
    }

    private InsuranceSourceReviewResponse insuranceSourceReview(InsurancePolicyType policyType) {
        return insuranceRequirementRules.findEffectiveRulesForPolicy(policyType, LocalDate.now(clock), PageRequest.of(0, 1)).stream()
            .findFirst()
            .map(InsuranceSourceReviewResponse::from)
            .orElseGet(() -> InsuranceSourceReviewResponse.missing(LocalDate.now(clock)));
    }

    private void addDocumentReminders(
        String driverId,
        ComplianceDocument document,
        List<ComplianceAlert> generatedAlerts,
        List<ComplianceCalendarItem> generatedCalendar
    ) {
        var status = document.getStatus();
        if (status != ComplianceItemStatus.EXPIRED && status != ComplianceItemStatus.EXPIRING_SOON) {
            return;
        }
        var severity = status == ComplianceItemStatus.EXPIRED ? ComplianceSeverity.CRITICAL : ComplianceSeverity.WARNING;
        var message = "Document " + document.getDocumentType() + " is " + status.name().toLowerCase().replace('_', ' ') + ".";
        generatedAlerts.add(new ComplianceAlert(
            driverId,
            ComplianceAlertType.DOCUMENT_EXPIRATION,
            severity,
            status,
            ComplianceSubjectType.DOCUMENT,
            document.getId(),
            document.getExpiresOn(),
            message,
            ComplianceWording.DOCUMENT_CAVEAT,
            ComplianceWording.RNTRC_OFFICIAL_ACTION_URL
        ));
        generatedCalendar.add(new ComplianceCalendarItem(
            driverId,
            ComplianceSubjectType.DOCUMENT,
            document.getId(),
            document.getTitle(),
            document.getExpiresOn(),
            status,
            severity,
            ComplianceWording.DOCUMENT_CAVEAT,
            ComplianceWording.RNTRC_OFFICIAL_ACTION_URL
        ));
    }
}
