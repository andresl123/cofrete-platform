package com.cofrete.dataimporter.messaging.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FuelPriceImportCompletedEvent(
    String eventType,
    int version,
    String eventId,
    String source,
    List<String> fuelTypes,
    LocalDate periodStart,
    LocalDate periodEnd,
    int recordsImported,
    String freshnessStatus,
    String importAuditId,
    String correlationId,
    Instant completedAt
) {
}
