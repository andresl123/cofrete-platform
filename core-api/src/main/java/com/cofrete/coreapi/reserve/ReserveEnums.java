package com.cofrete.coreapi.reserve;

enum ReserveBucket {
    FUEL_ARLA_TOLL_CASH_FLOW,
    MAINTENANCE,
    TIRES,
    INSURANCE,
    TAXES_AND_DOCUMENTS,
    TRUCK_REPLACEMENT,
    EMERGENCY,
    DRIVER_SALARY,
    PROFIT
}

enum ReserveRulePolicy {
    PERCENT_OF_AMOUNT,
    FIXED_AMOUNT,
    PER_KM
}

enum ReserveAllocationReason {
    FREIGHT_PAYMENT_RECEIVED,
    RESERVE_RULE_CHANGED,
    MANUAL_CORRECTION,
    RECEIVABLE_MARKED_PAID
}

enum ReserveAllocationStatus {
    REQUESTED,
    ALLOCATED
}

enum ReserveAllocationRequestStatus {
    REQUESTED,
    ALLOCATED,
    DUPLICATE_IGNORED
}

enum ReserveTransactionType {
    CREDIT,
    DEBIT
}

enum FinancialHealthStatus {
    GOOD,
    ATTENTION,
    RISK,
    UNKNOWN
}
