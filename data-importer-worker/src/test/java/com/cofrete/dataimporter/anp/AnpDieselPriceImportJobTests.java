package com.cofrete.dataimporter.anp;

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

class AnpDieselPriceImportJobTests {

    private final CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
    private final DataImportEventPublisher publisher = new DataImportEventPublisher(
        rabbitTemplate,
        new DataImporterRabbitProperties()
    );
    private final AnpDieselPriceImportJob job = new AnpDieselPriceImportJob(
        new AnpDieselPriceFixtureLoader(new ClassPathResource("fixtures/anp-diesel-prices-synthetic.csv")),
        publisher,
        Clock.fixed(Instant.parse("2026-05-11T12:00:20Z"), ZoneOffset.UTC)
    );

    @Test
    void returnsImportJobMetadataForSyntheticFixture() throws IOException {
        var result = job.loadSyntheticFixture("corr_123");

        assertThat(result.source()).isEqualTo("ANP");
        assertThat(result.dataset()).isEqualTo("synthetic-anp-diesel-price-fixture");
        assertThat(result.recordsImported()).isEqualTo(3);
        assertThat(result.freshnessStatus().name()).isEqualTo("CURRENT");
        assertThat(result.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
        assertThat(result.correlationId()).isEqualTo("corr_123");
        assertThat(result.completedAt()).isEqualTo(Instant.parse("2026-05-11T12:00:20Z"));
    }

    @Test
    void publishesCompletionEventForSyntheticFixture() throws IOException {
        var event = job.publishSyntheticFixtureCompleted("corr_123");

        assertThat(event.eventType()).isEqualTo(DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED);
        assertThat(event.version()).isEqualTo(1);
        assertThat(event.idempotencyKey()).startsWith("fuel-price:ANP:");
        assertThat(event.source()).isEqualTo("ANP");
        assertThat(event.dataset()).isEqualTo("synthetic-anp-diesel-price-fixture");
        assertThat(event.fuelTypes()).containsExactly("DIESEL_S10", "DIESEL_S500");
        assertThat(event.retrievedAt()).isEqualTo(Instant.parse("2026-05-11T12:00:20Z"));
        assertThat(event.recordsImported()).isEqualTo(3);
        assertThat(event.freshnessStatus()).isEqualTo("CURRENT");
        assertThat(event.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
        assertThat(event.fileHash()).isEqualTo("synthetic-fixture");
        assertThat(event.correlationId()).isEqualTo("corr_123");
        assertThat(event.producer()).isEqualTo("data-importer-worker");
        assertThat(event.completedAt()).isEqualTo(Instant.parse("2026-05-11T12:00:20Z"));
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
