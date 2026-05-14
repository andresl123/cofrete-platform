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

    @NotBlank
    private String reserveAllocationRequestedRoutingKey = "reserve.allocation.requested";

    @NotBlank
    private String reserveAllocationCompletedQueue = "core-api.reserve-allocation.completed";

    @NotBlank
    private String reserveAllocationCompletedRoutingKey = "reserve.allocation.completed";

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

    public String getReserveAllocationRequestedRoutingKey() {
        return reserveAllocationRequestedRoutingKey;
    }

    public void setReserveAllocationRequestedRoutingKey(String reserveAllocationRequestedRoutingKey) {
        this.reserveAllocationRequestedRoutingKey = reserveAllocationRequestedRoutingKey;
    }

    public String getReserveAllocationCompletedQueue() {
        return reserveAllocationCompletedQueue;
    }

    public void setReserveAllocationCompletedQueue(String reserveAllocationCompletedQueue) {
        this.reserveAllocationCompletedQueue = reserveAllocationCompletedQueue;
    }

    public String getReserveAllocationCompletedRoutingKey() {
        return reserveAllocationCompletedRoutingKey;
    }

    public void setReserveAllocationCompletedRoutingKey(String reserveAllocationCompletedRoutingKey) {
        this.reserveAllocationCompletedRoutingKey = reserveAllocationCompletedRoutingKey;
    }
}
