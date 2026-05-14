package com.cofrete.coreapi.trip;

enum TripDecisionState {
    DRAFT,
    ACCEPTED,
    REJECTED,
    RENEGOTIATION
}

enum AcceptanceDecisionValue {
    ACCEPT,
    REJECT,
    RENEGOTIATE
}

enum FinancialHealthStatus {
    GOOD,
    ATTENTION,
    RISK,
    UNKNOWN
}

enum ProfitabilityRecommendation {
    ACCEPT,
    RENEGOTIATE,
    REJECT
}

enum RecalculationReason {
    TRIP_CREATED,
    TRIP_UPDATED,
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    FUEL_ESTIMATE_UPDATED,
    TOLL_CLASSIFICATION_UPDATED,
    RESERVE_RULE_UPDATED,
    RECEIVABLE_UPDATED,
    IMPORT_DATA_REFRESHED,
    MANUAL_RECALCULATION
}

enum TollClassification {
    PASS_THROUGH,
    DRIVER_PAID_NON_REIMBURSED,
    INCLUDED_IN_FREIGHT,
    NO_TOLL,
    UNKNOWN
}

enum TollPaidBy {
    DRIVER,
    CONTRACTOR,
    SHIPPER,
    UNKNOWN
}

enum ValePedagioStatus {
    VALE_PEDAGIO_RECEIVED,
    VALE_PEDAGIO_NOT_CONFIRMED
}
