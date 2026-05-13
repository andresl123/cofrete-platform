package com.cofrete.dataimporter.imports;

import java.time.Instant;

public record ImportJobResult(
    String source,
    String dataset,
    int recordsImported,
    FreshnessStatus freshnessStatus,
    String confidence,
    String importAuditId,
    String correlationId,
    Instant completedAt
) {
}
