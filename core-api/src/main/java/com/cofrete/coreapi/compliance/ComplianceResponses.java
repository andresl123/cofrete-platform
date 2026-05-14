package com.cofrete.coreapi.compliance;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record ComplianceProfileEnvelope(ComplianceProfileResponse complianceProfile) {
}

record RntrcProfileEnvelope(RntrcProfileResponse rntrcProfile) {
}

record RntrcPublicStatusCheckEnvelope(RntrcPublicStatusCheckResponse publicStatusCheck) {
}

record InsurancePolicyEnvelope(InsurancePolicyResponse insurancePolicy) {
}

record InsurancePoliciesEnvelope(Iterable<InsurancePolicyResponse> insurancePolicies) {
}

record ComplianceScoreEnvelope(ComplianceScoreResponse complianceScore) {
}

record ComplianceCalendarEnvelope(Iterable<ComplianceCalendarItemResponse> calendarItems) {
}

record ComplianceDocumentEnvelope(ComplianceDocumentResponse document) {
}

record ComplianceDocumentsEnvelope(Iterable<ComplianceDocumentResponse> documents) {
}

record ComplianceProfileResponse(
    String driverId,
    RntrcProfileResponse rntrc,
    CiotResponse ciot,
    InsuranceSummaryResponse insurance,
    DocumentSummaryResponse documents,
    ComplianceScoreResponse score,
    List<ComplianceAlertResponse> alerts,
    List<String> caveats
) {
}

record RntrcProfileResponse(
    String numberMasked,
    RntrcCategory category,
    RntrcStatus status,
    String source,
    Instant lastCheckedAt,
    String officialActionUrl,
    String advisoryText,
    List<String> guidance
) {

    static RntrcProfileResponse from(ComplianceProfile profile) {
        return new RntrcProfileResponse(
            ComplianceResponseMasks.maskLast4(ComplianceResponseMasks.last4(profile.getRntrcNumber())),
            profile.getRntrcCategory(),
            profile.getRntrcStatus(),
            profile.getRntrcSource(),
            profile.getRntrcLastCheckedAt(),
            profile.getOfficialActionUrl(),
            profile.getAdvisoryText(),
            ComplianceWording.rntrcGuidance()
        );
    }
}

record CiotResponse(String required, String latestTripStatus, String advisoryText) {
}

record InsuranceSummaryResponse(int activePolicies, int expiringSoon, int expired, String advisoryText) {
}

record DocumentSummaryResponse(int activeDocuments, int expiringSoon, int expired, String advisoryText) {
}

record RntrcPublicStatusCheckResponse(
    String status,
    String source,
    String officialActionUrl,
    String advisoryText,
    List<String> guidance
) {
}

record InsurancePolicyResponse(
    String id,
    String driverId,
    String truckId,
    InsurancePolicyType policyType,
    String insurer,
    String brokerContact,
    String policyNumberMasked,
    LocalDate startsOn,
    LocalDate expiresOn,
    ComplianceItemStatus status,
    String annualPremium,
    String monthlyReserve,
    String currency,
    boolean linkedRntrc,
    boolean pgrRequired,
    VerificationStatus verificationStatus,
    String documentReference,
    boolean active,
    String advisoryText
) {

    static InsurancePolicyResponse from(InsurancePolicy policy) {
        return new InsurancePolicyResponse(
            policy.getId(),
            policy.getDriverId(),
            policy.getTruckId(),
            policy.getPolicyType(),
            policy.getInsurer(),
            policy.getBrokerContact(),
            ComplianceResponseMasks.maskLast4(policy.getPolicyNumberLast4()),
            policy.getStartsOn(),
            policy.getExpiresOn(),
            ComplianceDates.statusFor(policy.getExpiresOn()),
            policy.getAnnualPremium() == null ? null : policy.getAnnualPremium().toPlainString(),
            policy.getMonthlyReserve() == null ? null : policy.getMonthlyReserve().toPlainString(),
            policy.getCurrency(),
            policy.isLinkedRntrc(),
            policy.isPgrRequired(),
            policy.getVerificationStatus(),
            policy.getDocumentReference(),
            policy.isActive(),
            ComplianceWording.INSURANCE_CAVEAT
        );
    }
}

record ComplianceScoreResponse(ComplianceScore score, List<String> caveats) {
}

record ComplianceScore(
    int value,
    ComplianceScoreStatus status,
    Instant snapshotAt,
    List<ComplianceScoreComponent> components
) {
}

record ComplianceScoreComponent(String name, ComplianceItemStatus status, String weight, String advisoryText) {
}

record ComplianceCalendarItemResponse(
    String id,
    ComplianceSubjectType subjectType,
    String subjectId,
    String title,
    LocalDate dueOn,
    ComplianceItemStatus status,
    ComplianceSeverity severity,
    String advisoryText,
    String officialActionUrl
) {

    static ComplianceCalendarItemResponse from(ComplianceCalendarItem item) {
        return new ComplianceCalendarItemResponse(
            item.getId(),
            item.getSubjectType(),
            item.getSubjectId(),
            item.getTitle(),
            item.getDueOn(),
            item.getStatus(),
            item.getSeverity(),
            item.getAdvisoryText(),
            item.getOfficialActionUrl()
        );
    }
}

record ComplianceAlertResponse(
    String id,
    ComplianceAlertType alertType,
    ComplianceSeverity severity,
    ComplianceItemStatus status,
    ComplianceSubjectType subjectType,
    String subjectId,
    LocalDate dueOn,
    String message,
    String advisoryText,
    String officialActionUrl,
    Instant generatedAt
) {

    static ComplianceAlertResponse from(ComplianceAlert alert) {
        return new ComplianceAlertResponse(
            alert.getId(),
            alert.getAlertType(),
            alert.getSeverity(),
            alert.getStatus(),
            alert.getSubjectType(),
            alert.getSubjectId(),
            alert.getDueOn(),
            alert.getMessage(),
            alert.getAdvisoryText(),
            alert.getOfficialActionUrl(),
            alert.getGeneratedAt()
        );
    }
}

record ComplianceDocumentResponse(
    String id,
    String driverId,
    ComplianceDocumentType documentType,
    ComplianceOwnerType ownerType,
    String ownerId,
    String title,
    String identifierMasked,
    LocalDate issuedOn,
    LocalDate expiresOn,
    ComplianceSource source,
    ComplianceItemStatus status,
    String storageObjectKey,
    String storageContentType,
    String notes,
    boolean active,
    String advisoryText
) {

    static ComplianceDocumentResponse from(ComplianceDocument document) {
        var currentStatus = ComplianceDates.statusFor(document.getExpiresOn());
        return new ComplianceDocumentResponse(
            document.getId(),
            document.getDriverId(),
            document.getDocumentType(),
            document.getOwnerType(),
            document.getOwnerId(),
            document.getTitle(),
            ComplianceResponseMasks.maskLast4(document.getIdentifierLast4()),
            document.getIssuedOn(),
            document.getExpiresOn(),
            document.getSource(),
            currentStatus,
            document.getStorageObjectKey(),
            document.getStorageContentType(),
            document.getNotes(),
            document.isActive(),
            ComplianceWording.DOCUMENT_CAVEAT
        );
    }
}

final class ComplianceResponseMasks {

    private ComplianceResponseMasks() {
    }

    static String last4(String value) {
        if (value == null || value.length() < 4) {
            return null;
        }
        return value.substring(value.length() - 4);
    }

    static String maskLast4(String last4) {
        return last4 == null ? null : "***" + last4;
    }
}
