package com.cofrete.coreapi.reserve;

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

@Entity
@Table(name = "reserve_transactions")
class ReserveTransaction {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private ReserveWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_id")
    private ReserveAllocation allocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ReserveBucket bucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReserveTransactionType transactionType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 60)
    private String sourceType;

    @Column(nullable = false, length = 160)
    private String sourceReference;

    @Column(length = 240)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReserveTransaction() {
    }

    ReserveTransaction(
        String accountId,
        ReserveWallet wallet,
        ReserveAllocation allocation,
        BigDecimal amount,
        String sourceReference
    ) {
        id = DomainIds.prefixed("reserve_txn");
        this.accountId = accountId;
        this.wallet = wallet;
        this.allocation = allocation;
        bucket = wallet.getBucket();
        transactionType = ReserveTransactionType.CREDIT;
        this.amount = amount;
        balanceAfter = wallet.getCurrentBalance();
        currency = wallet.getCurrency();
        sourceType = "RESERVE_ALLOCATION";
        this.sourceReference = sourceReference;
        note = "Virtual reserve ledger movement. Not a real money transfer.";
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    String getId() {
        return id;
    }

    ReserveBucket getBucket() {
        return bucket;
    }

    ReserveTransactionType getTransactionType() {
        return transactionType;
    }

    BigDecimal getAmount() {
        return amount;
    }

    BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    String getCurrency() {
        return currency;
    }

    String getSourceType() {
        return sourceType;
    }

    String getSourceReference() {
        return sourceReference;
    }

    String getNote() {
        return note;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
