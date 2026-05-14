package com.cofrete.coreapi.compliance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

record RntrcMetadataRequest(
    @Size(max = 40) String rntrcNumber,
    RntrcCategory rntrcCategory,
    RntrcStatus rntrcStatus,
    Instant lastCheckedAt,
    CiotRequiredStatus ciotRequired,
    CiotTripStatus latestCiotStatus
) {
}

record RntrcPublicStatusCheckRequest(
    @NotBlank @Size(max = 40) String rntrcNumber,
    RntrcCategory rntrcCategory,
    Boolean consentToPublicLookup
) {
}

record InsurancePolicyRequest(
    @NotNull InsurancePolicyType policyType,
    @NotBlank @Size(max = 160) String insurer,
    @Size(max = 160) String brokerContact,
    @Pattern(regexp = "\\d{4}") String policyNumberLast4,
    LocalDate startsOn,
    LocalDate expiresOn,
    @DecimalMin("0.00") BigDecimal annualPremium,
    @DecimalMin("0.00") BigDecimal monthlyReserve,
    @Pattern(regexp = "BRL") String currency,
    Boolean linkedRntrc,
    Boolean pgrRequired,
    VerificationStatus verificationStatus,
    @Size(max = 160) String documentReference,
    @Size(max = 64) String truckId,
    Boolean active
) {
}

record ComplianceDocumentRequest(
    @NotNull ComplianceDocumentType documentType,
    ComplianceOwnerType ownerType,
    @Size(max = 64) String ownerId,
    @NotBlank @Size(max = 160) String title,
    @Pattern(regexp = "\\d{4}") String identifierLast4,
    LocalDate issuedOn,
    LocalDate expiresOn,
    ComplianceSource source,
    @Size(max = 300) String storageObjectKey,
    @Size(max = 120) String storageContentType,
    @Size(max = 500) String notes,
    Boolean active
) {
}

record WaitingTimeRuleRequest(
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal thresholdHours,
    @NotNull @DecimalMin("0.00") BigDecimal ratePerTonHour,
    @Pattern(regexp = "BRL") String currency,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Size(max = 500) String sourceUrl,
    Instant reviewedAt,
    ComplianceRuleFreshnessStatus freshnessStatus,
    RuleConfidence confidence
) {
}

record InsuranceRequirementRuleRequest(
    @NotBlank @Size(max = 120) String requirementScope,
    @NotNull InsurancePolicyType policyType,
    Boolean required,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Size(max = 160) String sourceName,
    @Size(max = 500) String sourceUrl,
    Instant reviewedAt,
    ComplianceRuleFreshnessStatus freshnessStatus,
    RuleConfidence confidence,
    @Size(max = 700) String notes
) {
}
