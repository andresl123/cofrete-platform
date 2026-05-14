package com.cofrete.coreapi.receivables;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "receivables")
class Receivable {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 64)
    private String tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceivableType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceivableStatus status = ReceivableStatus.EXPECTED;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO.setScale(2);

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceivablePaymentMethod paymentMethod;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Receivable() {
    }

    Receivable(String accountId, Customer customer, ReceivableRequest request) {
        id = DomainIds.prefixed("recv");
        this.accountId = accountId;
        this.customer = customer;
        tripId = blankToNull(request.tripId());
        type = request.type();
        amount = ReceivableMoney.positiveMoney(request.amount(), "amount");
        currency = request.currency();
        dueDate = request.dueDate();
        paymentMethod = request.paymentMethod();
        note = blankToNull(request.note());
    }

    ReceivableStatus refreshLateStatus(LocalDate today) {
        if (status == ReceivableStatus.EXPECTED && dueDate.isBefore(today)) {
            status = ReceivableStatus.LATE;
        }
        return status;
    }

    ReceivableStatus applyPayment(BigDecimal paymentAmount) {
        paidAmount = paidAmount.add(paymentAmount).setScale(2);
        status = paidAmount.compareTo(amount) >= 0 ? ReceivableStatus.PAID : ReceivableStatus.PARTIALLY_PAID;
        return status;
    }

    BigDecimal remainingAmount() {
        return amount.subtract(paidAmount).max(BigDecimal.ZERO).setScale(2);
    }

    boolean isOpen() {
        return status != ReceivableStatus.PAID && status != ReceivableStatus.CANCELED;
    }

    boolean isOverdue(LocalDate today) {
        return isOpen() && dueDate.isBefore(today);
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    Customer getCustomer() {
        return customer;
    }

    String getTripId() {
        return tripId;
    }

    ReceivableType getType() {
        return type;
    }

    ReceivableStatus getStatus() {
        return status;
    }

    BigDecimal getAmount() {
        return amount;
    }

    BigDecimal getPaidAmount() {
        return paidAmount;
    }

    String getCurrency() {
        return currency;
    }

    LocalDate getDueDate() {
        return dueDate;
    }

    ReceivablePaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    String getNote() {
        return note;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
