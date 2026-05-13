package com.cofrete.dataimporter.anp;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import com.cofrete.dataimporter.imports.ImportJobResult;
import com.cofrete.dataimporter.messaging.DataImportEventNames;
import com.cofrete.dataimporter.messaging.DataImportEventPublisher;
import com.cofrete.dataimporter.messaging.events.FuelPriceImportCompletedEvent;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AnpDieselPriceImportJob {

    private static final String SOURCE = "ANP";
    private static final String DATASET = "synthetic-anp-diesel-price-fixture";

    private final AnpDieselPriceFixtureLoader fixtureLoader;
    private final DataImportEventPublisher publisher;
    private final Clock clock;

    public AnpDieselPriceImportJob(
        AnpDieselPriceFixtureLoader fixtureLoader,
        DataImportEventPublisher publisher,
        Clock clock
    ) {
        this.fixtureLoader = fixtureLoader;
        this.publisher = publisher;
        this.clock = clock;
    }

    public ImportJobResult loadSyntheticFixture(String correlationId) throws IOException {
        List<AnpDieselPriceFixtureRecord> records = fixtureLoader.load();
        FreshnessStatus freshnessStatus = records.stream()
            .map(AnpDieselPriceFixtureRecord::freshnessStatus)
            .findFirst()
            .orElse(FreshnessStatus.UNKNOWN);
        String confidence = records.stream()
            .map(AnpDieselPriceFixtureRecord::confidence)
            .findFirst()
            .orElse("UNKNOWN");

        return new ImportJobResult(
            SOURCE,
            DATASET,
            records.size(),
            freshnessStatus,
            confidence,
            "import_" + UUID.randomUUID(),
            correlationId,
            Instant.now(clock)
        );
    }

    public FuelPriceImportCompletedEvent publishSyntheticFixtureCompleted(String correlationId) throws IOException {
        List<AnpDieselPriceFixtureRecord> records = fixtureLoader.load();
        AnpDieselPriceFixtureRecord firstRecord = records.stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("ANP diesel fixture must contain at least one record"));

        FuelPriceImportCompletedEvent event = new FuelPriceImportCompletedEvent(
            DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED,
            1,
            "evt_" + UUID.randomUUID(),
            "fuel-price:" + SOURCE + ":" + firstRecord.periodStart() + ":" + firstRecord.periodEnd() + ":synthetic-fixture",
            SOURCE,
            DATASET,
            records.stream().map(AnpDieselPriceFixtureRecord::fuelType).distinct().sorted().toList(),
            firstRecord.periodStart(),
            firstRecord.periodEnd(),
            Instant.now(clock),
            records.size(),
            firstRecord.freshnessStatus().name(),
            firstRecord.confidence(),
            "import_" + UUID.randomUUID(),
            "synthetic-fixture",
            correlationId,
            "data-importer-worker",
            Instant.now(clock)
        );

        publisher.publishFuelPriceImportCompleted(event);
        return event;
    }
}
