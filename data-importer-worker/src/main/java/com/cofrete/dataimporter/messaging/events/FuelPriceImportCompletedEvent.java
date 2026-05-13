package com.cofrete.dataimporter.messaging.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FuelPriceImportCompletedEvent(
    String eventType,
    int version,
    String eventId,
    String idempotencyKey,
    String source,
    String dataset,
    List<String> fuelTypes,
    LocalDate periodStart,
    LocalDate periodEnd,
    Instant retrievedAt,
    int recordsImported,
    String freshnessStatus,
    String confidence,
    String importAuditId,
    String fileHash,
    String correlationId,
    String producer,
    Instant completedAt
) {
}
