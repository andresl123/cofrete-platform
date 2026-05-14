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
import java.time.Instant;

@Entity
@Table(name = "receivable_status_changes")
class ReceivableStatusChange {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_id", nullable = false)
    private Receivable receivable;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ReceivableStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceivableStatus newStatus;

    @Column(nullable = false, length = 80)
    private String reason;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(length = 500)
    private String note;

    protected ReceivableStatusChange() {
    }

    ReceivableStatusChange(
        String accountId,
        Receivable receivable,
        ReceivableStatus previousStatus,
        ReceivableStatus newStatus,
        String reason,
        String note
    ) {
        id = DomainIds.prefixed("recv_status");
        this.accountId = accountId;
        this.receivable = receivable;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.note = note;
    }

    @PrePersist
    void onCreate() {
        changedAt = Instant.now();
    }

    String getId() {
        return id;
    }

    ReceivableStatus getPreviousStatus() {
        return previousStatus;
    }

    ReceivableStatus getNewStatus() {
        return newStatus;
    }

    String getReason() {
        return reason;
    }

    Instant getChangedAt() {
        return changedAt;
    }

    String getNote() {
        return note;
    }
}
