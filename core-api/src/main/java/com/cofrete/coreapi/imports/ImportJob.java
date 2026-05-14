package com.cofrete.coreapi.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "import_jobs")
class ImportJob {

    @Id
    private String id;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 120)
    private String dataset;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 500)
    private String sourceUrl;

    private LocalDate sourcePeriodStart;

    private LocalDate sourcePeriodEnd;

    @Column(nullable = false)
    private Instant retrievedAt;

    private Instant completedAt;

    @Column(nullable = false)
    private int rowCount;

    @Column(nullable = false, length = 40)
    private String freshnessStatus;

    @Column(nullable = false, length = 80)
    private String confidence;

    @Column(length = 160)
    private String fileHash;

    @Column(length = 1000)
    private String parserErrorSummary;

    @Column(length = 160)
    private String correlationId;

    @Column(nullable = false)
    private Instant createdAt;

    protected ImportJob() {
    }

    String getId() {
        return id;
    }

    String getSource() {
        return source;
    }

    String getDataset() {
        return dataset;
    }

    String getSourceUrl() {
        return sourceUrl;
    }

    LocalDate getSourcePeriodStart() {
        return sourcePeriodStart;
    }

    LocalDate getSourcePeriodEnd() {
        return sourcePeriodEnd;
    }

    Instant getRetrievedAt() {
        return retrievedAt;
    }

    Instant getCompletedAt() {
        return completedAt;
    }

    int getRowCount() {
        return rowCount;
    }

    String getFreshnessStatus() {
        return freshnessStatus;
    }

    String getConfidence() {
        return confidence;
    }

    String getFileHash() {
        return fileHash;
    }

    String getParserErrorSummary() {
        return parserErrorSummary;
    }
}
