package com.cofrete.coreapi.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "compliance_profiles")
class ComplianceProfile {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(length = 40)
    private String rntrcNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private RntrcCategory rntrcCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RntrcStatus rntrcStatus;

    @Column(nullable = false, length = 80)
    private String rntrcSource;

    private Instant rntrcLastCheckedAt;

    @Column(length = 500)
    private String officialActionUrl;

    @Column(nullable = false, length = 500)
    private String advisoryText;

    @Column(nullable = false, length = 40)
    private String ciotRequired;

    @Column(nullable = false, length = 40)
    private String latestCiotStatus;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceProfile() {
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
