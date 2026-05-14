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
@Table(name = "compliance_profiles")
class ComplianceProfile {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(length = 40)
    private String rntrcNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private RntrcCategory rntrcCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RntrcStatus rntrcStatus = RntrcStatus.UNKNOWN;

    @Column(nullable = false, length = 80)
    private String rntrcSource = "driver_entered";

    private Instant rntrcLastCheckedAt;

    @Column(length = 500)
    private String officialActionUrl = ComplianceWording.RNTRC_OFFICIAL_ACTION_URL;

    @Column(nullable = false, length = 500)
    private String advisoryText = ComplianceWording.RNTRC_NOT_OFFICIAL_RECORD;

    @Column(nullable = false, length = 40)
    private String ciotRequired = "UNKNOWN";

    @Column(nullable = false, length = 40)
    private String latestCiotStatus = "NOT_RECORDED";

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceProfile() {
    }

    ComplianceProfile(String driverId) {
        id = DomainIds.prefixed("compliance_profile");
        this.driverId = driverId;
    }

    void updateFrom(RntrcMetadataRequest request) {
        rntrcNumber = blankToNull(request.rntrcNumber());
        rntrcCategory = request.rntrcCategory() == null ? RntrcCategory.UNKNOWN : request.rntrcCategory();
        rntrcStatus = request.rntrcStatus() == null ? RntrcStatus.UNKNOWN : request.rntrcStatus();
        rntrcSource = "driver_entered";
        rntrcLastCheckedAt = request.lastCheckedAt();
        officialActionUrl = ComplianceWording.RNTRC_OFFICIAL_ACTION_URL;
        advisoryText = ComplianceWording.RNTRC_NOT_OFFICIAL_RECORD;
        ciotRequired = request.ciotRequired() == null ? "UNKNOWN" : request.ciotRequired().name();
        latestCiotStatus = request.latestCiotStatus() == null ? "NOT_RECORDED" : request.latestCiotStatus().name();
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

    String getDriverId() {
        return driverId;
    }

    String getRntrcNumber() {
        return rntrcNumber;
    }

    RntrcCategory getRntrcCategory() {
        return rntrcCategory;
    }

    RntrcStatus getRntrcStatus() {
        return rntrcStatus;
    }

    String getRntrcSource() {
        return rntrcSource;
    }

    Instant getRntrcLastCheckedAt() {
        return rntrcLastCheckedAt;
    }

    String getOfficialActionUrl() {
        return officialActionUrl;
    }

    String getAdvisoryText() {
        return advisoryText;
    }

    String getCiotRequired() {
        return ciotRequired;
    }

    String getLatestCiotStatus() {
        return latestCiotStatus;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
