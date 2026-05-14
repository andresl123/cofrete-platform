package com.cofrete.coreapi.receivables;

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
@Table(name = "customers")
class Customer {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerTaxIdType taxIdType;

    @Column(length = 4)
    private String taxIdLast4;

    @Column(length = 120)
    private String contactName;

    @Column(length = 40)
    private String contactPhone;

    @Column(nullable = false)
    private int paymentTermsDays;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Customer() {
    }

    Customer(String accountId, CustomerRequest request) {
        id = DomainIds.prefixed("cust");
        this.accountId = accountId;
        name = request.name().trim();
        taxIdType = request.taxIdType();
        taxIdLast4 = blankToNull(request.taxIdLast4());
        contactName = blankToNull(request.contactName());
        contactPhone = blankToNull(request.contactPhone());
        paymentTermsDays = request.paymentTermsDays();
        notes = blankToNull(request.notes());
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

    String getAccountId() {
        return accountId;
    }

    String getName() {
        return name;
    }

    CustomerTaxIdType getTaxIdType() {
        return taxIdType;
    }

    String getTaxIdLast4() {
        return taxIdLast4;
    }

    String getContactName() {
        return contactName;
    }

    String getContactPhone() {
        return contactPhone;
    }

    int getPaymentTermsDays() {
        return paymentTermsDays;
    }

    String getNotes() {
        return notes;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
