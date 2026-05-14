package com.cofrete.dataimporter.antt;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import com.cofrete.dataimporter.imports.ImportJobResult;
import com.cofrete.dataimporter.messaging.DataImportEventNames;
import com.cofrete.dataimporter.messaging.DataImportEventPublisher;
import com.cofrete.dataimporter.messaging.events.TollDataImportCompletedEvent;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AnttTollDataImportJob {

    private static final String SOURCE = "ANTT_DADOS_ABERTOS";
    private static final String DATASET = "toll_plazas_and_tariffs";
    private static final String FILE_HASH = "synthetic-fixture";

    private final AnttTollTariffFixtureLoader fixtureLoader;
    private final DataImportEventPublisher publisher;
    private final Clock clock;

    public AnttTollDataImportJob(
        AnttTollTariffFixtureLoader fixtureLoader,
        DataImportEventPublisher publisher,
        Clock clock
    ) {
        this.fixtureLoader = fixtureLoader;
        this.publisher = publisher;
        this.clock = clock;
    }

    public ImportJobResult loadSyntheticFixture(String correlationId) throws IOException {
        List<AnttTollTariffFixtureRecord> records = fixtureLoader.load();
        AnttTollTariffFixtureRecord firstRecord = firstRecord(records);

        return new ImportJobResult(
            SOURCE,
            DATASET,
            firstRecord.sourceUrl(),
            firstRecord.effectiveStart(),
            firstRecord.effectiveEnd(),
            records.size(),
            firstRecord.freshnessStatus(),
            firstRecord.confidence(),
            "import_" + UUID.randomUUID(),
            FILE_HASH,
            null,
            correlationId,
            Instant.now(clock)
        );
    }

    public TollDataImportCompletedEvent publishSyntheticFixtureCompleted(String correlationId) throws IOException {
        List<AnttTollTariffFixtureRecord> records = fixtureLoader.load();
        AnttTollTariffFixtureRecord firstRecord = firstRecord(records);
        Instant now = Instant.now(clock);

        TollDataImportCompletedEvent event = new TollDataImportCompletedEvent(
            DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED,
            1,
            "evt_" + UUID.randomUUID(),
            "toll-data:" + SOURCE + ":" + firstRecord.effectiveStart() + ":" + firstRecord.effectiveEnd() + ":" + FILE_HASH,
            SOURCE,
            DATASET,
            firstRecord.effectiveStart(),
            firstRecord.effectiveEnd(),
            now,
            records.size(),
            List.of("CSV"),
            firstRecord.freshnessStatus().name(),
            firstRecord.confidence(),
            "import_" + UUID.randomUUID(),
            FILE_HASH,
            correlationId,
            "data-importer-worker",
            now
        );

        publisher.publishTollDataImportCompleted(event);
        return event;
    }

    private static AnttTollTariffFixtureRecord firstRecord(List<AnttTollTariffFixtureRecord> records) {
        return records.stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("ANTT toll fixture must contain at least one record"));
    }
}
