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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "receivable_payments")
class ReceivablePayment {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_id", nullable = false)
    private Receivable receivable;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceivablePaymentMethod paymentMethod;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReceivablePayment() {
    }

    ReceivablePayment(String accountId, Receivable receivable, MarkReceivablePaidRequest request, BigDecimal amount) {
        id = DomainIds.prefixed("recv_pay");
        this.accountId = accountId;
        this.receivable = receivable;
        this.amount = amount;
        currency = request.currency();
        paidDate = request.paidDate();
        paymentMethod = request.paymentMethod();
        note = request.note() == null || request.note().isBlank() ? null : request.note().trim();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    String getId() {
        return id;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    LocalDate getPaidDate() {
        return paidDate;
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
}
