package com.cofrete.coreapi.imports;

import java.time.Instant;

public record TollImportStatus(
    String source,
    String dataset,
    String sourceUrl,
    String sourcePeriodStart,
    String sourcePeriodEnd,
    String freshnessStatus,
    String confidence,
    String fileHash,
    String parserErrorSummary,
    int rowCount,
    Instant retrievedAt,
    Instant completedAt,
    String importAuditId
) {
}
