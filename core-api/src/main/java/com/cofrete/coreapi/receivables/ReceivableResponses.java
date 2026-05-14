package com.cofrete.coreapi.receivables;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record CustomerEnvelope(CustomerResponse customer) {
}

record ReceivableEnvelope(ReceivableResponse receivable) {
}

record ReceivablesEnvelope(List<ReceivableResponse> receivables, ReceivableTotalsResponse totals) {
}

record CustomerProfitabilityEnvelope(CustomerProfitabilityResponse customerProfitability) {
}

record CustomerResponse(
    String id,
    String name,
    CustomerTaxIdType taxIdType,
    String taxIdLast4,
    String contactName,
    String contactPhone,
    int paymentTermsDays,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {

    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getTaxIdType(),
            customer.getTaxIdLast4(),
            customer.getContactName(),
            customer.getContactPhone(),
            customer.getPaymentTermsDays(),
            customer.getNotes(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}

record ReceivableResponse(
    String id,
    String customerId,
    String tripId,
    ReceivableType type,
    ReceivableStatus status,
    String amount,
    String paidAmount,
    String remainingAmount,
    String currency,
    LocalDate dueDate,
    ReceivablePaymentMethod paymentMethod,
    String note,
    List<ReceivablePaymentResponse> payments,
    List<ReceivableStatusChangeResponse> statusChanges,
    String advisoryText,
    Instant createdAt,
    Instant updatedAt
) {

    static ReceivableResponse from(
        Receivable receivable,
        List<ReceivablePayment> payments,
        List<ReceivableStatusChange> statusChanges
    ) {
        return new ReceivableResponse(
            receivable.getId(),
            receivable.getCustomer().getId(),
            receivable.getTripId(),
            receivable.getType(),
            receivable.getStatus(),
            ReceivableMoney.money(receivable.getAmount()),
            ReceivableMoney.money(receivable.getPaidAmount()),
            ReceivableMoney.money(receivable.remainingAmount()),
            receivable.getCurrency(),
            receivable.getDueDate(),
            receivable.getPaymentMethod(),
            receivable.getNote(),
            payments.stream().map(ReceivablePaymentResponse::from).toList(),
            statusChanges.stream().map(ReceivableStatusChangeResponse::from).toList(),
            "Receivables are cash-flow planning records only. Cofrete does not move money or guarantee payment.",
            receivable.getCreatedAt(),
            receivable.getUpdatedAt()
        );
    }
}

record ReceivablePaymentResponse(
    String id,
    String amount,
    String currency,
    LocalDate paidDate,
    ReceivablePaymentMethod paymentMethod,
    String note,
    Instant createdAt
) {

    static ReceivablePaymentResponse from(ReceivablePayment payment) {
        return new ReceivablePaymentResponse(
            payment.getId(),
            ReceivableMoney.money(payment.getAmount()),
            payment.getCurrency(),
            payment.getPaidDate(),
            payment.getPaymentMethod(),
            payment.getNote(),
            payment.getCreatedAt()
        );
    }
}

record ReceivableStatusChangeResponse(
    String id,
    ReceivableStatus previousStatus,
    ReceivableStatus newStatus,
    String reason,
    Instant changedAt,
    String note
) {

    static ReceivableStatusChangeResponse from(ReceivableStatusChange change) {
        return new ReceivableStatusChangeResponse(
            change.getId(),
            change.getPreviousStatus(),
            change.getNewStatus(),
            change.getReason(),
            change.getChangedAt(),
            change.getNote()
        );
    }
}

record ReceivableTotalsResponse(
    String expectedAmount,
    String lateAmount,
    String partiallyPaidAmount,
    String paidAmount,
    String currency
) {

    static ReceivableTotalsResponse from(List<Receivable> receivables) {
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal late = BigDecimal.ZERO;
        BigDecimal partial = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        for (Receivable receivable : receivables) {
            switch (receivable.getStatus()) {
                case EXPECTED -> expected = expected.add(receivable.remainingAmount());
                case LATE -> late = late.add(receivable.remainingAmount());
                case PARTIALLY_PAID -> partial = partial.add(receivable.remainingAmount());
                case PAID -> paid = paid.add(receivable.getPaidAmount());
                case CANCELED -> {
                }
            }
        }
        return new ReceivableTotalsResponse(
            ReceivableMoney.money(expected),
            ReceivableMoney.money(late),
            ReceivableMoney.money(partial),
            ReceivableMoney.money(paid),
            ReceivableMoney.BRL
        );
    }
}

record CustomerProfitabilityResponse(
    String customerId,
    LocalDate periodStart,
    LocalDate periodEnd,
    String grossFreight,
    String expectedProfit,
    String averagePaymentDelayDays,
    String lateReceivables,
    String currency,
    ReceivableRiskStatus receivableRiskStatus,
    ReceivableRiskInputsResponse receivableRiskInputs,
    CustomerQualitySignalsResponse qualitySignals,
    String advisoryText
) {
}

record ReceivableRiskInputsResponse(
    int receivableCount,
    int paidReceivableCount,
    int lateReceivableCount,
    String openReceivableAmount,
    String overdueReceivableAmount,
    String maxPaymentDelayDays
) {
}

record CustomerQualitySignalsResponse(
    String ciotStatus,
    String valePedagioStatus,
    String loadingDelayStatus,
    ReceivableRiskStatus paymentDelayStatus
) {
}

record ProfitabilityTotals(
    BigDecimal grossFreight,
    BigDecimal expectedProfit,
    BigDecimal lateReceivables,
    BigDecimal openReceivables,
    BigDecimal delayDays,
    BigDecimal maxDelayDays,
    int receivableCount,
    int paidCount,
    int lateCount
) {

    String averageDelayDays() {
        if (paidCount == 0) {
            return "0.00";
        }
        return delayDays.divide(new BigDecimal(paidCount), 2, RoundingMode.HALF_UP).toPlainString();
    }
}
