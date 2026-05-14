package com.cofrete.coreapi.compliance;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "compliance_score_snapshots")
class ComplianceScoreSnapshot {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceScoreStatus status;

    @Column(nullable = false, columnDefinition = "text")
    private String componentSummary;

    @Column(nullable = false, columnDefinition = "text")
    private String caveats;

    @Column(nullable = false)
    private Instant snapshotAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceScoreSnapshot() {
    }

    ComplianceScoreSnapshot(String driverId, ComplianceScoreResponse response) {
        id = DomainIds.prefixed("compliance_score");
        this.driverId = driverId;
        score = response.score().value();
        status = response.score().status();
        componentSummary = response.score().components().toString();
        caveats = response.caveats().toString();
        snapshotAt = response.score().snapshotAt();
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
}
