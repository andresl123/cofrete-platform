package com.cofrete.dataimporter.messaging;

import com.cofrete.dataimporter.messaging.events.FuelPriceImportCompletedEvent;
import com.cofrete.dataimporter.messaging.events.TollDataImportCompletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DataImportEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final DataImporterRabbitProperties properties;

    public DataImportEventPublisher(RabbitTemplate rabbitTemplate, DataImporterRabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishFuelPriceImportCompleted(FuelPriceImportCompletedEvent event) {
        Assert.isTrue(
            DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED.equals(event.eventType()),
            () -> "Expected eventType " + DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED
                + " but received " + event.eventType()
        );

        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getFuelPriceImportCompletedRoutingKey(),
            event
        );
    }

    public void publishTollDataImportCompleted(TollDataImportCompletedEvent event) {
        Assert.isTrue(
            DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED.equals(event.eventType()),
            () -> "Expected eventType " + DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED
                + " but received " + event.eventType()
        );

        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getTollDataImportCompletedRoutingKey(),
            event
        );
    }
}
