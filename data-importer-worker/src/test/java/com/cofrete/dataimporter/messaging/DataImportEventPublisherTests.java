package com.cofrete.dataimporter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cofrete.dataimporter.messaging.events.FuelPriceImportCompletedEvent;
import com.cofrete.dataimporter.messaging.events.TollDataImportCompletedEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class DataImportEventPublisherTests {

    private final CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
    private final DataImporterRabbitProperties properties = new DataImporterRabbitProperties();
    private final DataImportEventPublisher publisher = new DataImportEventPublisher(rabbitTemplate, properties);

    @Test
    void publishesFuelPriceImportCompletedWithCanonicalRoutingKey() {
        FuelPriceImportCompletedEvent event = fuelPriceEvent(DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED);

        publisher.publishFuelPriceImportCompleted(event);

        assertThat(rabbitTemplate.exchange).isEqualTo(properties.getExchange());
        assertThat(rabbitTemplate.routingKey).isEqualTo(DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED);
        assertThat(rabbitTemplate.message).isSameAs(event);
    }

    @Test
    void publishesTollDataImportCompletedWithCanonicalRoutingKey() {
        TollDataImportCompletedEvent event = tollDataEvent(DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED);

        publisher.publishTollDataImportCompleted(event);

        assertThat(rabbitTemplate.exchange).isEqualTo(properties.getExchange());
        assertThat(rabbitTemplate.routingKey).isEqualTo(DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED);
        assertThat(rabbitTemplate.message).isSameAs(event);
    }

    @Test
    void rejectsUnexpectedFuelPriceEventName() {
        FuelPriceImportCompletedEvent event = fuelPriceEvent("fuel-prices.import.completed");

        assertThatThrownBy(() -> publisher.publishFuelPriceImportCompleted(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED);
    }

    @Test
    void rejectsUnexpectedTollDataEventName() {
        TollDataImportCompletedEvent event = tollDataEvent("toll.import.completed");

        assertThatThrownBy(() -> publisher.publishTollDataImportCompleted(event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED);
    }

    private static FuelPriceImportCompletedEvent fuelPriceEvent(String eventType) {
        return new FuelPriceImportCompletedEvent(
            eventType,
            1,
            "evt_126",
            "fuel-price:ANP:2026-05-03:2026-05-09:sha256-example",
            "ANP",
            "diesel_prices",
            List.of("DIESEL_S10"),
            LocalDate.parse("2026-05-03"),
            LocalDate.parse("2026-05-09"),
            Instant.parse("2026-05-11T11:59:00Z"),
            12345,
            "CURRENT",
            "official_weekly",
            "import_123",
            "sha256-example",
            "corr_123",
            "data-importer-worker",
            Instant.parse("2026-05-11T12:00:20Z")
        );
    }

    private static TollDataImportCompletedEvent tollDataEvent(String eventType) {
        return new TollDataImportCompletedEvent(
            eventType,
            1,
            "evt_127",
            "toll-data:ANTT_DADOS_ABERTOS:2026-05-01:2026-05-31:sha256-example",
            "ANTT_DADOS_ABERTOS",
            "toll_plazas_and_tariffs",
            LocalDate.parse("2026-05-01"),
            LocalDate.parse("2026-05-31"),
            Instant.parse("2026-05-11T11:58:00Z"),
            500,
            List.of("CSV", "JSON"),
            "CURRENT",
            "official_or_open_dataset",
            "import_124",
            "sha256-example",
            "corr_123",
            "data-importer-worker",
            Instant.parse("2026-05-11T12:00:30Z")
        );
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {

        private String exchange;
        private String routingKey;
        private Object message;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }
    }
}
