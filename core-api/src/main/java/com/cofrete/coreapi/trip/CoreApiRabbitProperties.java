package com.cofrete.coreapi.trip;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cofrete.core-api.rabbitmq")
public class CoreApiRabbitProperties {

    private boolean enabled = true;

    @NotBlank
    private String exchange = "cofrete.finance.events";

    @NotBlank
    private String tripRecalculationRequestedRoutingKey = TripRecalculationEventRecord.EVENT_TYPE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getTripRecalculationRequestedRoutingKey() {
        return tripRecalculationRequestedRoutingKey;
    }

    public void setTripRecalculationRequestedRoutingKey(String tripRecalculationRequestedRoutingKey) {
        this.tripRecalculationRequestedRoutingKey = tripRecalculationRequestedRoutingKey;
    }
}
