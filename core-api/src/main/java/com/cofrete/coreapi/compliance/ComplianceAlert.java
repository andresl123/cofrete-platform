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
@Table(name = "compliance_alerts")
class ComplianceAlert {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ComplianceAlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceSubjectType subjectType;

    @Column(length = 64)
    private String subjectId;

    private LocalDate dueOn;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(nullable = false, length = 700)
    private String advisoryText;

    @Column(length = 500)
    private String officialActionUrl;

    @Column(nullable = false)
    private Instant generatedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceAlert() {
    }

    ComplianceAlert(
        String driverId,
        ComplianceAlertType alertType,
        ComplianceSeverity severity,
        ComplianceItemStatus status,
        ComplianceSubjectType subjectType,
        String subjectId,
        LocalDate dueOn,
        String message,
        String advisoryText,
        String officialActionUrl
    ) {
        id = DomainIds.prefixed("alert");
        this.driverId = driverId;
        this.alertType = alertType;
        this.severity = severity;
        this.status = status;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.dueOn = dueOn;
        this.message = message;
        this.advisoryText = advisoryText;
        this.officialActionUrl = officialActionUrl;
        generatedAt = Instant.now();
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

    ComplianceAlertType getAlertType() {
        return alertType;
    }

    ComplianceSeverity getSeverity() {
        return severity;
    }

    ComplianceItemStatus getStatus() {
        return status;
    }

    ComplianceSubjectType getSubjectType() {
        return subjectType;
    }

    String getSubjectId() {
        return subjectId;
    }

    LocalDate getDueOn() {
        return dueOn;
    }

    String getMessage() {
        return message;
    }

    String getAdvisoryText() {
        return advisoryText;
    }

    String getOfficialActionUrl() {
        return officialActionUrl;
    }

    Instant getGeneratedAt() {
        return generatedAt;
    }
}
