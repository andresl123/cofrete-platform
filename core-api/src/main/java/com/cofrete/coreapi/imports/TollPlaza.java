package com.cofrete.coreapi.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "toll_plazas")
class TollPlaza {

    @Id
    private String id;

    @Column(nullable = false, length = 120)
    private String externalId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String highway;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(length = 120)
    private String municipality;

    @Column(precision = 8, scale = 2)
    private BigDecimal kmMarker;

    @Column(length = 40)
    private String direction;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 80)
    private String sourceType;

    @Column(length = 500)
    private String sourceUrl;

    @Column(nullable = false, length = 40)
    private String freshnessStatus;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(nullable = false, length = 64)
    private String importAuditId;

    @Column(nullable = false)
    private Instant createdAt;

    protected TollPlaza() {
    }

    String getName() {
        return name;
    }

    String getHighway() {
        return highway;
    }

    String getState() {
        return state;
    }

    String getSource() {
        return source;
    }

    String getSourceType() {
        return sourceType;
    }

    String getSourceUrl() {
        return sourceUrl;
    }

    String getFreshnessStatus() {
        return freshnessStatus;
    }

    String getConfidence() {
        return confidence;
    }

    String getImportAuditId() {
        return importAuditId;
    }
}
