package com.cofrete.coreapi.trip;

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
@Table(name = "trip_acceptance_decisions")
class TripAcceptanceDecision {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AcceptanceDecisionValue decision;

    @Column(length = 500)
    private String reasonCodes;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant decidedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected TripAcceptanceDecision() {
    }

    TripAcceptanceDecision(Trip trip, AcceptanceDecisionRequest request) {
        id = DomainIds.prefixed("trip_decision");
        this.trip = trip;
        decision = request.decision();
        reasonCodes = String.join(",", request.reasonCodes());
        note = request.note() == null || request.note().isBlank() ? null : request.note().trim();
        decidedAt = request.decidedAt() == null ? Instant.now() : request.decidedAt();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    String getId() {
        return id;
    }

    AcceptanceDecisionValue getDecision() {
        return decision;
    }

    String getReasonCodes() {
        return reasonCodes;
    }

    String getNote() {
        return note;
    }

    Instant getDecidedAt() {
        return decidedAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
