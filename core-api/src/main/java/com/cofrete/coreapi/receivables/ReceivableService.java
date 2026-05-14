package com.cofrete.coreapi.receivables;

import com.cofrete.coreapi.auth.AppUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ReceivableService {

    private static final String STATUS_OVERDUE = "overdue";

    private final CustomerRepository customers;
    private final ReceivableRepository receivables;
    private final ReceivablePaymentRepository payments;
    private final ReceivableStatusChangeRepository statusChanges;

    ReceivableService(
        CustomerRepository customers,
        ReceivableRepository receivables,
        ReceivablePaymentRepository payments,
        ReceivableStatusChangeRepository statusChanges
    ) {
        this.customers = customers;
        this.receivables = receivables;
        this.payments = payments;
        this.statusChanges = statusChanges;
    }

    @Transactional
    CustomerResponse createCustomer(AppUser user, CustomerRequest request) {
        validateCustomer(request);
        try {
            return CustomerResponse.from(customers.saveAndFlush(new Customer(user.getAccountId(), request)));
        } catch (DataIntegrityViolationException exception) {
            throw new ReceivableConflictException("A customer with this name already exists for this account.");
        }
    }

    @Transactional
    ReceivableResponse createReceivable(AppUser user, ReceivableRequest request) {
        validateReceivableRequest(request);
        var customer = requireCustomer(user, request.customerId());
        var receivable = receivables.save(new Receivable(user.getAccountId(), customer, request));
        statusChanges.save(new ReceivableStatusChange(
            user.getAccountId(),
            receivable,
            null,
            receivable.getStatus(),
            "RECEIVABLE_CREATED",
            request.note()
        ));
        return response(receivable);
    }

    @Transactional
    CustomerProfitabilityResponse customerProfitability(AppUser user, String customerId) {
        var customer = requireCustomer(user, customerId);
        var today = LocalDate.now();
        var customerReceivables = receivables.findByCustomerOrderByDueDateAscCreatedAtAsc(customer);
        var refreshed = customerReceivables.stream()
            .peek(receivable -> refreshLateStatus(user, receivable, today))
            .toList();
        var totals = profitabilityTotals(refreshed, today);
        var risk = riskStatus(totals);
        var periodStart = refreshed.stream().map(Receivable::getDueDate).min(LocalDate::compareTo).orElse(null);
        var periodEnd = refreshed.stream().map(Receivable::getDueDate).max(LocalDate::compareTo).orElse(null);
        return new CustomerProfitabilityResponse(
            customer.getId(),
            periodStart,
            periodEnd,
            ReceivableMoney.money(totals.grossFreight()),
            ReceivableMoney.money(totals.expectedProfit()),
            totals.averageDelayDays(),
            ReceivableMoney.money(totals.lateReceivables()),
            ReceivableMoney.BRL,
            risk,
            new ReceivableRiskInputsResponse(
                totals.receivableCount(),
                totals.paidCount(),
                totals.lateCount(),
                ReceivableMoney.money(totals.openReceivables()),
                ReceivableMoney.money(totals.lateReceivables()),
                totals.maxDelayDays().toPlainString()
            ),
            new CustomerQualitySignalsResponse("UNKNOWN", "UNKNOWN", "UNKNOWN", risk),
            "Customer profitability is based on Cofrete receivable records and may not include all external obligations."
        );
    }

    @Transactional
    ReceivablesEnvelope listReceivables(AppUser user, String status) {
        var today = LocalDate.now();
        List<Receivable> results;
        if (status == null || status.isBlank()) {
            results = receivables.findByAccountIdOrderByDueDateAscCreatedAtAsc(user.getAccountId());
        } else if (STATUS_OVERDUE.equalsIgnoreCase(status)) {
            results = receivables.findByAccountIdAndDueDateBeforeAndStatusInOrderByDueDateAscCreatedAtAsc(
                user.getAccountId(),
                today,
                List.of(ReceivableStatus.EXPECTED, ReceivableStatus.LATE, ReceivableStatus.PARTIALLY_PAID)
            );
        } else {
            ReceivableStatus requestedStatus = parseStatus(status);
            results = receivables.findByAccountIdOrderByDueDateAscCreatedAtAsc(user.getAccountId()).stream()
                .filter(receivable -> receivable.getStatus() == requestedStatus)
                .toList();
        }
        results.forEach(receivable -> refreshLateStatus(user, receivable, today));
        var responses = results.stream().map(this::response).toList();
        return new ReceivablesEnvelope(responses, ReceivableTotalsResponse.from(results));
    }

    @Transactional
    ReceivableResponse markPaid(AppUser user, String receivableId, MarkReceivablePaidRequest request) {
        validatePaymentRequest(request);
        var receivable = requireReceivable(user, receivableId);
        if (!receivable.isOpen()) {
            throw new ReceivableConflictException("Receivable is not open for payment.");
        }
        var amount = ReceivableMoney.positiveMoney(request.paidAmount(), "paidAmount");
        if (amount.compareTo(receivable.remainingAmount()) > 0) {
            throw new ReceivableValidationException("paidAmount cannot exceed the remaining receivable amount.");
        }
        refreshLateStatus(user, receivable, LocalDate.now());
        var previous = receivable.getStatus();
        payments.save(new ReceivablePayment(user.getAccountId(), receivable, request, amount));
        var next = receivable.applyPayment(amount);
        if (previous != next) {
            statusChanges.save(new ReceivableStatusChange(
                user.getAccountId(),
                receivable,
                previous,
                next,
                "PAYMENT_RECORDED",
                request.note()
            ));
        }
        return response(receivable);
    }

    private void refreshLateStatus(AppUser user, Receivable receivable, LocalDate today) {
        var previous = receivable.getStatus();
        var next = receivable.refreshLateStatus(today);
        if (previous != next) {
            statusChanges.save(new ReceivableStatusChange(
                user.getAccountId(),
                receivable,
                previous,
                next,
                "DUE_DATE_PASSED",
                "Receivable became overdue during cash-flow planning review."
            ));
        }
    }

    private Customer requireCustomer(AppUser user, String customerId) {
        return customers.findByAccountIdAndId(user.getAccountId(), customerId)
            .orElseThrow(() -> new ReceivableNotFoundException("Customer not found."));
    }

    private Receivable requireReceivable(AppUser user, String receivableId) {
        return receivables.findByAccountIdAndId(user.getAccountId(), receivableId)
            .orElseThrow(() -> new ReceivableNotFoundException("Receivable not found."));
    }

    private ReceivableResponse response(Receivable receivable) {
        return ReceivableResponse.from(
            receivable,
            payments.findByReceivableOrderByCreatedAtAsc(receivable),
            statusChanges.findByReceivableOrderByChangedAtAsc(receivable)
        );
    }

    private static void validateCustomer(CustomerRequest request) {
        if (request.taxIdLast4() != null && request.taxIdType() == null) {
            throw new ReceivableValidationException("taxIdType is required when taxIdLast4 is provided.");
        }
    }

    private static void validateReceivableRequest(ReceivableRequest request) {
        validateCurrency(request.currency());
        if (request.dueDate().isBefore(LocalDate.of(2020, 1, 1))) {
            throw new ReceivableValidationException("dueDate is outside the supported planning range.");
        }
    }

    private static void validatePaymentRequest(MarkReceivablePaidRequest request) {
        validateCurrency(request.currency());
    }

    private static void validateCurrency(String currency) {
        if (!ReceivableMoney.BRL.equals(currency)) {
            throw new ReceivableValidationException("currency must be BRL.");
        }
    }

    private static ReceivableStatus parseStatus(String status) {
        try {
            return ReceivableStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ReceivableValidationException("status must be overdue or a known receivable status.");
        }
    }

    private ProfitabilityTotals profitabilityTotals(List<Receivable> receivables, LocalDate today) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal expectedProfit = BigDecimal.ZERO;
        BigDecimal late = BigDecimal.ZERO;
        BigDecimal open = BigDecimal.ZERO;
        BigDecimal delayDays = BigDecimal.ZERO;
        BigDecimal maxDelay = BigDecimal.ZERO;
        int paidCount = 0;
        int lateCount = 0;
        for (Receivable receivable : receivables) {
            gross = gross.add(receivable.getAmount());
            if (receivable.isOpen()) {
                open = open.add(receivable.remainingAmount());
            }
            if (receivable.isOverdue(today)) {
                late = late.add(receivable.remainingAmount());
                lateCount += 1;
            }
            if (receivable.getStatus() == ReceivableStatus.PAID) {
                paidCount += 1;
                var latestPaymentDate = payments.findByReceivableOrderByCreatedAtAsc(receivable).stream()
                    .map(ReceivablePayment::getPaidDate)
                    .max(LocalDate::compareTo);
                BigDecimal delay = latestPaymentDate
                    .map(paidDate -> new BigDecimal(Math.max(0, ChronoUnit.DAYS.between(receivable.getDueDate(), paidDate))))
                    .orElse(BigDecimal.ZERO);
                delayDays = delayDays.add(delay);
                maxDelay = maxDelay.max(delay);
            }
        }
        return new ProfitabilityTotals(
            gross.setScale(2),
            expectedProfit.setScale(2),
            late.setScale(2),
            open.setScale(2),
            delayDays,
            maxDelay,
            receivables.size(),
            paidCount,
            lateCount
        );
    }

    private static ReceivableRiskStatus riskStatus(ProfitabilityTotals totals) {
        if (totals.receivableCount() == 0) {
            return ReceivableRiskStatus.UNKNOWN;
        }
        if (totals.lateCount() > 0 || totals.lateReceivables().compareTo(BigDecimal.ZERO) > 0) {
            return ReceivableRiskStatus.HIGH;
        }
        if (totals.openReceivables().compareTo(BigDecimal.ZERO) > 0) {
            return ReceivableRiskStatus.ATTENTION;
        }
        return ReceivableRiskStatus.LOW;
    }
}
