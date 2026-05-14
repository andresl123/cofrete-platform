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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "insurance_policies")
class InsurancePolicy {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String driverId;

    @Column(length = 64)
    private String truckId;

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
    private String currency = "BRL";

    @Column(nullable = false)
    private boolean linkedRntrc;

    @Column(nullable = false)
    private boolean pgrRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VerificationStatus verificationStatus = VerificationStatus.UNKNOWN;

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

    InsurancePolicy(String driverId, String truckId, InsurancePolicyRequest request) {
        id = DomainIds.prefixed("policy");
        this.driverId = driverId;
        this.truckId = truckId;
        updateFrom(request, truckId);
    }

    void updateFrom(InsurancePolicyRequest request, String truckId) {
        this.truckId = truckId;
        policyType = request.policyType();
        insurer = request.insurer().trim();
        brokerContact = blankToNull(request.brokerContact());
        policyNumberLast4 = request.policyNumberLast4();
        startsOn = request.startsOn();
        expiresOn = request.expiresOn();
        annualPremium = request.annualPremium();
        monthlyReserve = request.monthlyReserve();
        currency = request.currency() == null ? "BRL" : request.currency();
        linkedRntrc = request.linkedRntrc() != null && request.linkedRntrc();
        pgrRequired = request.pgrRequired() != null && request.pgrRequired();
        verificationStatus = request.verificationStatus() == null ? VerificationStatus.UNKNOWN : request.verificationStatus();
        documentReference = blankToNull(request.documentReference());
        active = request.active() == null || request.active();
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

    String getTruckId() {
        return truckId;
    }

    InsurancePolicyType getPolicyType() {
        return policyType;
    }

    String getInsurer() {
        return insurer;
    }

    String getBrokerContact() {
        return brokerContact;
    }

    String getPolicyNumberLast4() {
        return policyNumberLast4;
    }

    LocalDate getStartsOn() {
        return startsOn;
    }

    LocalDate getExpiresOn() {
        return expiresOn;
    }

    BigDecimal getAnnualPremium() {
        return annualPremium;
    }

    BigDecimal getMonthlyReserve() {
        return monthlyReserve;
    }

    String getCurrency() {
        return currency;
    }

    boolean isLinkedRntrc() {
        return linkedRntrc;
    }

    boolean isPgrRequired() {
        return pgrRequired;
    }

    VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    String getDocumentReference() {
        return documentReference;
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
