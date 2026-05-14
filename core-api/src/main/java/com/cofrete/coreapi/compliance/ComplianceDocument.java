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
@Table(name = "compliance_documents")
class ComplianceDocument {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ComplianceDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceOwnerType ownerType;

    @Column(length = 64)
    private String ownerId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 4)
    private String identifierLast4;

    private LocalDate issuedOn;

    private LocalDate expiresOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private ComplianceSource source = ComplianceSource.DRIVER_ENTERED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceItemStatus status = ComplianceItemStatus.UNKNOWN;

    @Column(length = 300)
    private String storageObjectKey;

    @Column(length = 120)
    private String storageContentType;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ComplianceDocument() {
    }

    ComplianceDocument(String driverId, String ownerId, ComplianceDocumentRequest request) {
        id = DomainIds.prefixed("doc");
        this.driverId = driverId;
        updateFrom(ownerId, request);
    }

    void updateFrom(String ownerId, ComplianceDocumentRequest request) {
        documentType = request.documentType();
        ownerType = request.ownerType() == null ? ComplianceOwnerType.DRIVER : request.ownerType();
        this.ownerId = ownerId;
        title = request.title().trim();
        identifierLast4 = request.identifierLast4();
        issuedOn = request.issuedOn();
        expiresOn = request.expiresOn();
        source = request.source() == null ? ComplianceSource.DRIVER_ENTERED : request.source();
        status = ComplianceDates.statusFor(expiresOn);
        storageObjectKey = blankToNull(request.storageObjectKey());
        storageContentType = blankToNull(request.storageContentType());
        notes = blankToNull(request.notes());
        active = request.active() == null || request.active();
    }

    void refreshStatus(java.time.Clock clock) {
        status = ComplianceDates.statusFor(expiresOn, clock);
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

    ComplianceDocumentType getDocumentType() {
        return documentType;
    }

    ComplianceOwnerType getOwnerType() {
        return ownerType;
    }

    String getOwnerId() {
        return ownerId;
    }

    String getTitle() {
        return title;
    }

    String getIdentifierLast4() {
        return identifierLast4;
    }

    LocalDate getIssuedOn() {
        return issuedOn;
    }

    LocalDate getExpiresOn() {
        return expiresOn;
    }

    ComplianceSource getSource() {
        return source;
    }

    ComplianceItemStatus getStatus() {
        return status;
    }

    String getStorageObjectKey() {
        return storageObjectKey;
    }

    String getStorageContentType() {
        return storageContentType;
    }

    String getNotes() {
        return notes;
    }

    boolean isActive() {
        return active;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
