package com.cofrete.coreapi.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "insurance_policies")
class InsurancePolicy {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    private Truck truck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private InsurancePolicyType policyType;

    @Column(nullable = false, length = 160)
    private String insurer;

    @Column(length = 160)
    private String brokerContact;

    @Column(length = 4)
    private String policyNumberLast4;

    private LocalDate startsOn;

    private LocalDate expiresOn;

    @Column(precision = 14, scale = 2)
    private BigDecimal annualPremium;

    @Column(precision = 14, scale = 2)
    private BigDecimal monthlyReserve;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean linkedRntrc;

    @Column(nullable = false)
    private boolean pgrRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VerificationStatus verificationStatus;

    @Column(length = 160)
    private String documentReference;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected InsurancePolicy() {
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
