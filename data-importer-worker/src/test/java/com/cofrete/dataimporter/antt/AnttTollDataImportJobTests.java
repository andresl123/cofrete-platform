package com.cofrete.dataimporter.antt;

import static org.assertj.core.api.Assertions.assertThat;

import com.cofrete.dataimporter.messaging.DataImportEventNames;
import com.cofrete.dataimporter.messaging.DataImportEventPublisher;
import com.cofrete.dataimporter.messaging.DataImporterRabbitProperties;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;

class AnttTollDataImportJobTests {

    private final CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
    private final DataImportEventPublisher publisher = new DataImportEventPublisher(
        rabbitTemplate,
        new DataImporterRabbitProperties()
    );
    private final AnttTollDataImportJob job = new AnttTollDataImportJob(
        new AnttTollTariffFixtureLoader(new ClassPathResource("fixtures/antt-toll-plazas-tariffs-synthetic.csv")),
        publisher,
        Clock.fixed(Instant.parse("2026-05-11T12:00:30Z"), ZoneOffset.UTC)
    );

    @Test
    void returnsImportJobMetadataForSyntheticFixture() throws IOException {
        var result = job.loadSyntheticFixture("corr_antt");

        assertThat(result.source()).isEqualTo("ANTT_DADOS_ABERTOS");
        assertThat(result.dataset()).isEqualTo("toll_plazas_and_tariffs");
        assertThat(result.sourceUrl()).isEqualTo("https://dados.antt.gov.br/dataset/praca-de-pedagio");
        assertThat(result.periodStart().toString()).isEqualTo("2026-05-01");
        assertThat(result.periodEnd().toString()).isEqualTo("2026-05-31");
        assertThat(result.recordsImported()).isEqualTo(2);
        assertThat(result.freshnessStatus().name()).isEqualTo("CURRENT");
        assertThat(result.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
        assertThat(result.fileHash()).isEqualTo("synthetic-fixture");
        assertThat(result.parserErrorSummary()).isNull();
        assertThat(result.correlationId()).isEqualTo("corr_antt");
        assertThat(result.completedAt()).isEqualTo(Instant.parse("2026-05-11T12:00:30Z"));
    }

    @Test
    void publishesCompletionEventForSyntheticFixture() throws IOException {
        var event = job.publishSyntheticFixtureCompleted("corr_antt");

        assertThat(event.eventType()).isEqualTo(DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED);
        assertThat(event.version()).isEqualTo(1);
        assertThat(event.idempotencyKey()).isEqualTo(
            "toll-data:ANTT_DADOS_ABERTOS:2026-05-01:2026-05-31:synthetic-fixture"
        );
        assertThat(event.source()).isEqualTo("ANTT_DADOS_ABERTOS");
        assertThat(event.dataset()).isEqualTo("toll_plazas_and_tariffs");
        assertThat(event.recordsImported()).isEqualTo(2);
        assertThat(event.formats()).containsExactly("CSV");
        assertThat(event.freshnessStatus()).isEqualTo("CURRENT");
        assertThat(event.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
        assertThat(event.fileHash()).isEqualTo("synthetic-fixture");
        assertThat(event.correlationId()).isEqualTo("corr_antt");
        assertThat(event.producer()).isEqualTo("data-importer-worker");
        assertThat(event.completedAt()).isEqualTo(Instant.parse("2026-05-11T12:00:30Z"));
        assertThat(rabbitTemplate.message).isSameAs(event);
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        private Object message;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            this.message = message;
        }
    }
}
