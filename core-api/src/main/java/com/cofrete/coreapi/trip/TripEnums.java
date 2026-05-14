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
