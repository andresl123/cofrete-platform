package com.cofrete.dataimporter.messaging.events;

import java.time.Instant;
import java.util.List;

public record TollDataImportCompletedEvent(
    String eventType,
    int version,
    String eventId,
    String source,
    int recordsImported,
    List<String> formats,
    String freshnessStatus,
    String importAuditId,
    String correlationId,
    Instant completedAt
) {
}
