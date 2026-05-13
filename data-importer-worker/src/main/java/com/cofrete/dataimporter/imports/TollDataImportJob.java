package com.cofrete.dataimporter.imports;

import com.cofrete.dataimporter.messaging.DataImportEventNames;
import com.cofrete.dataimporter.messaging.DataImportEventPublisher;
import com.cofrete.dataimporter.messaging.events.TollDataImportCompletedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TollDataImportJob {

    private final DataImportEventPublisher publisher;
    private final Clock clock;

    public TollDataImportJob(DataImportEventPublisher publisher, Clock clock) {
        this.publisher = publisher;
        this.clock = clock;
    }

    public TollDataImportCompletedEvent publishOpenDataStubCompleted(String correlationId) {
        TollDataImportCompletedEvent event = new TollDataImportCompletedEvent(
            DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED,
            1,
            "evt_" + UUID.randomUUID(),
            "ANTT_DADOS_ABERTOS",
            0,
            List.of("CSV", "JSON"),
            FreshnessStatus.UNKNOWN.name(),
            "import_" + UUID.randomUUID(),
            correlationId,
            Instant.now(clock)
        );

        publisher.publishTollDataImportCompleted(event);
        return event;
    }
}
