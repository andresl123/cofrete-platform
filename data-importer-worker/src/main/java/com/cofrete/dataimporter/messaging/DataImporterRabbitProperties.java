package com.cofrete.dataimporter.messaging;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cofrete.data-importer.rabbitmq")
public class DataImporterRabbitProperties {

    @NotBlank
    private String exchange = "cofrete.import.events";

    @NotBlank
    private String fuelPriceImportCompletedRoutingKey = DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED;

    @NotBlank
    private String tollDataImportCompletedRoutingKey = DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getFuelPriceImportCompletedRoutingKey() {
        return fuelPriceImportCompletedRoutingKey;
    }

    public void setFuelPriceImportCompletedRoutingKey(String fuelPriceImportCompletedRoutingKey) {
        this.fuelPriceImportCompletedRoutingKey = fuelPriceImportCompletedRoutingKey;
    }

    public String getTollDataImportCompletedRoutingKey() {
        return tollDataImportCompletedRoutingKey;
    }

    public void setTollDataImportCompletedRoutingKey(String tollDataImportCompletedRoutingKey) {
        this.tollDataImportCompletedRoutingKey = tollDataImportCompletedRoutingKey;
    }
}
