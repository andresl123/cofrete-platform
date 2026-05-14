package com.cofrete.dataimporter.imports;

import java.time.Instant;
import java.time.LocalDate;

public record ImportJobResult(
    String source,
    String dataset,
    String sourceUrl,
    LocalDate periodStart,
    LocalDate periodEnd,
    int recordsImported,
    FreshnessStatus freshnessStatus,
    String confidence,
    String importAuditId,
    String fileHash,
    String parserErrorSummary,
    String correlationId,
    Instant completedAt
) {
}
