package com.cofrete.coreapi.receivables;

enum CustomerTaxIdType {
    CPF,
    CNPJ,
    UNKNOWN
}

enum ReceivableType {
    ADVANCE,
    BALANCE,
    REIMBURSEMENT,
    OTHER
}

enum ReceivableStatus {
    EXPECTED,
    PAID,
    LATE,
    PARTIALLY_PAID,
    CANCELED
}

enum ReceivablePaymentMethod {
    PIX,
    BOLETO,
    BANK_TRANSFER,
    CASH,
    OTHER
}

enum ReceivableRiskStatus {
    LOW,
    ATTENTION,
    HIGH,
    UNKNOWN
}
