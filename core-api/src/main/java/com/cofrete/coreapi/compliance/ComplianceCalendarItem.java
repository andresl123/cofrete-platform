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
import java.time.LocalDate;

@Entity
@Table(name = "compliance_calendar_items")
class ComplianceCalendarItem {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceSubjectType subjectType;

    @Column(length = 64)
    private String subjectId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private LocalDate dueOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceSeverity severity;

    @Column(nullable = false, length = 700)
    private String advisoryText;

    @Column(length = 500)
    private String officialActionUrl;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceCalendarItem() {
    }

    ComplianceCalendarItem(
        String driverId,
        ComplianceSubjectType subjectType,
        String subjectId,
        String title,
        LocalDate dueOn,
        ComplianceItemStatus status,
        ComplianceSeverity severity,
        String advisoryText,
        String officialActionUrl
    ) {
        id = DomainIds.prefixed("calendar");
        this.driverId = driverId;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.title = title;
        this.dueOn = dueOn;
        this.status = status;
        this.severity = severity;
        this.advisoryText = advisoryText;
        this.officialActionUrl = officialActionUrl;
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

    ComplianceSubjectType getSubjectType() {
        return subjectType;
    }

    String getSubjectId() {
        return subjectId;
    }

    String getTitle() {
        return title;
    }

    LocalDate getDueOn() {
        return dueOn;
    }

    ComplianceItemStatus getStatus() {
        return status;
    }

    ComplianceSeverity getSeverity() {
        return severity;
    }

    String getAdvisoryText() {
        return advisoryText;
    }

    String getOfficialActionUrl() {
        return officialActionUrl;
    }
}
