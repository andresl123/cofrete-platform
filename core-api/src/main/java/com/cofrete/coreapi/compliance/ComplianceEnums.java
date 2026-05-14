package com.cofrete.coreapi.compliance;

enum RntrcCategory {
    TAC,
    ETC,
    CTC,
    UNKNOWN
}

enum RntrcStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    EXPIRED,
    CANCELLED,
    UNKNOWN
}

enum InsurancePolicyType {
    RCTR_C,
    RC_DC,
    RC_V,
    TRUCK_HULL,
    LIFE_ACCIDENT,
    OTHER
}

enum VerificationStatus {
    VERIFIED_BY_DRIVER,
    PENDING_REVIEW,
    EXPIRED,
    UNKNOWN
}

enum ComplianceDocumentType {
    RNTRC,
    INSURANCE_POLICY,
    CNH,
    CRLV,
    IPVA,
    LICENSING,
    CIOT,
    TAX,
    OTHER
}

enum ComplianceOwnerType {
    DRIVER,
    TRUCK,
    INSURANCE_POLICY,
    OTHER
}

enum ComplianceSource {
    DRIVER_ENTERED,
    IMPORTED,
    COFRETE_GENERATED
}

enum ComplianceItemStatus {
    CURRENT,
    EXPIRING_SOON,
    EXPIRED,
    UNKNOWN
}

enum ComplianceSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum ComplianceAlertType {
    RNTRC_STATUS,
    INSURANCE_EXPIRATION,
    DOCUMENT_EXPIRATION
}

enum ComplianceSubjectType {
    RNTRC,
    INSURANCE_POLICY,
    DOCUMENT
}

enum ComplianceScoreStatus {
    GOOD,
    ATTENTION,
    RISK
}

enum CiotRequiredStatus {
    YES,
    NO,
    UNKNOWN
}

enum CiotTripStatus {
    RECORDED,
    MISSING,
    NOT_RECORDED,
    UNKNOWN
}
