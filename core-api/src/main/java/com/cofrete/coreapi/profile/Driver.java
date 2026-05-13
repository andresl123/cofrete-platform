package com.cofrete.coreapi.profile;

import com.cofrete.coreapi.auth.AppUser;
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
@Table(name = "drivers")
class Driver {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 320)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false, length = 2)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DocumentType documentType;

    @Column(length = 4)
    private String cpfCnpjLast4;

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

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Driver() {
    }

    Driver(AppUser user, CreateDriverRequest request) {
        id = DomainIds.prefixed("driver");
        this.user = user;
        updateFrom(request);
    }

    void updateFrom(CreateDriverRequest request) {
        name = request.name().trim();
        email = blankToNull(request.email());
        phone = blankToNull(request.phone());
        state = request.state().toUpperCase();
        documentType = request.documentType();
        cpfCnpjLast4 = request.cpfCnpjLast4();
        rntrcNumber = blankToNull(request.rntrcNumber());
        rntrcCategory = request.rntrcCategory();
        rntrcStatus = request.rntrcStatus() == null ? RntrcStatus.UNKNOWN : request.rntrcStatus();
        active = request.active() == null || request.active();
    }

    void patch(PatchDriverRequest request) {
        if (request.name() != null) {
            name = request.name().trim();
        }
        if (request.email() != null) {
            email = blankToNull(request.email());
        }
        if (request.phone() != null) {
            phone = blankToNull(request.phone());
        }
        if (request.state() != null) {
            state = request.state().toUpperCase();
        }
        if (request.documentType() != null) {
            documentType = request.documentType();
        }
        if (request.cpfCnpjLast4() != null) {
            cpfCnpjLast4 = request.cpfCnpjLast4();
        }
        if (request.rntrcNumber() != null) {
            rntrcNumber = blankToNull(request.rntrcNumber());
        }
        if (request.rntrcCategory() != null) {
            rntrcCategory = request.rntrcCategory();
        }
        if (request.rntrcStatus() != null) {
            rntrcStatus = request.rntrcStatus();
        }
        if (request.active() != null) {
            active = request.active();
        }
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

    AppUser getUser() {
        return user;
    }

    String getName() {
        return name;
    }

    String getEmail() {
        return email;
    }

    String getPhone() {
        return phone;
    }

    String getState() {
        return state;
    }

    DocumentType getDocumentType() {
        return documentType;
    }

    String getCpfCnpjLast4() {
        return cpfCnpjLast4;
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
