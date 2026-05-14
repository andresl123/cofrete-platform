package com.cofrete.coreapi.profile;

enum DocumentType {
    CPF,
    CNPJ,
    UNKNOWN
}

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

enum VehicleType {
    TRUCK,
    TRACTOR_UNIT,
    VAN,
    OTHER
}

enum TrailerType {
    SEMI_TRAILER,
    FULL_TRAILER,
    DOLLY,
    OTHER
}

enum FuelType {
    DIESEL_S10,
    DIESEL_S500,
    BIODIESEL,
    OTHER
}

enum TaxRegime {
    MEI_CAMINHONEIRO,
    PESSOA_FISICA_AUTONOMA,
    SIMPLES_NACIONAL,
    UNKNOWN
}

enum FreshnessStatus {
    CURRENT,
    STALE,
    FAILED,
    UNKNOWN
}

enum RuleStatus {
    CURRENT,
    STALE,
    MISSING,
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
