package com.cofrete.coreapi.auth;

import com.cofrete.coreapi.profile.DomainIds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 320)
    private String principalName;

    @Column(nullable = false, length = 40)
    private String userType;

    @Column(nullable = false, length = 40)
    private String role;

    @Column(nullable = false, unique = true, length = 64)
    private String accountId;

    @Column(length = 64)
    private String companyId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AppUser() {
    }

    public AppUser(String principalName) {
        this.id = DomainIds.prefixed("user");
        this.principalName = principalName;
        this.userType = "DRIVER";
        this.role = "DRIVER";
        this.accountId = DomainIds.prefixed("acct");
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

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }
}
