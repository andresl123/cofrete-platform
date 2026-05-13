package com.cofrete.coreapi.reserve;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reserve_wallets")
class ReserveWallet {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ReserveBucket bucket;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal targetBalance;

    @Column(nullable = false, length = 3)
    private String currency = "BRL";

    private Instant lastAllocationAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ReserveWallet() {
    }

    ReserveWallet(String accountId, ReserveBucket bucket, String currency) {
        id = DomainIds.prefixed("reserve_wallet");
        this.accountId = accountId;
        this.bucket = bucket;
        this.currency = currency;
    }

    void setTargetBalance(BigDecimal targetBalance) {
        this.targetBalance = targetBalance;
    }

    void credit(BigDecimal amount, Instant allocatedAt) {
        currentBalance = currentBalance.add(amount);
        lastAllocationAt = allocatedAt;
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

    String getId() {
        return id;
    }

    String getAccountId() {
        return accountId;
    }

    ReserveBucket getBucket() {
        return bucket;
    }

    BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    BigDecimal getTargetBalance() {
        return targetBalance;
    }

    String getCurrency() {
        return currency;
    }

    Instant getLastAllocationAt() {
        return lastAllocationAt;
    }
}
