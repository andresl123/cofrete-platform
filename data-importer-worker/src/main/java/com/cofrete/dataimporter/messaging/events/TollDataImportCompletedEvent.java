package com.cofrete.dataimporter.messaging.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TollDataImportCompletedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String source,
    String dataset,
    LocalDate periodStart,
    LocalDate periodEnd,
    Instant retrievedAt,
    int recordsImported,
    List<String> formats,
    String freshnessStatus,
    String confidence,
    String importAuditId,
    String fileHash,
    String correlationId,
    String producer,
    Instant completedAt
) {
}
