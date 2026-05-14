package com.cofrete.financeworker.messaging;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cofrete.finance-worker.rabbitmq")
public class FinanceWorkerRabbitProperties {

    @NotBlank
    private String exchange = "cofrete.finance.events";

    @NotBlank
    private String tripRecalculationQueue = "finance-worker.trip-recalculation.requested";

    @NotBlank
    private String reserveAllocationQueue = "finance-worker.reserve-allocation.requested";

    @NotBlank
    private String tripRecalculationRequestedRoutingKey = FinanceEventNames.TRIP_RECALCULATION_REQUESTED;

    @NotBlank
    private String reserveAllocationRequestedRoutingKey = FinanceEventNames.RESERVE_ALLOCATION_REQUESTED;

    @NotBlank
    private String tripFinanceRecalculatedRoutingKey = FinanceEventNames.TRIP_FINANCE_RECALCULATED;

    @NotBlank
    private String reserveAllocationCompletedRoutingKey = FinanceEventNames.RESERVE_ALLOCATION_COMPLETED;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getTripRecalculationQueue() {
        return tripRecalculationQueue;
    }

    public void setTripRecalculationQueue(String tripRecalculationQueue) {
        this.tripRecalculationQueue = tripRecalculationQueue;
    }

    public String getReserveAllocationQueue() {
        return reserveAllocationQueue;
    }

    public void setReserveAllocationQueue(String reserveAllocationQueue) {
        this.reserveAllocationQueue = reserveAllocationQueue;
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

    public String getTripFinanceRecalculatedRoutingKey() {
        return tripFinanceRecalculatedRoutingKey;
    }

    public void setTripFinanceRecalculatedRoutingKey(String tripFinanceRecalculatedRoutingKey) {
        this.tripFinanceRecalculatedRoutingKey = tripFinanceRecalculatedRoutingKey;
    }

    public String getReserveAllocationCompletedRoutingKey() {
        return reserveAllocationCompletedRoutingKey;
    }

    public void setReserveAllocationCompletedRoutingKey(String reserveAllocationCompletedRoutingKey) {
        this.reserveAllocationCompletedRoutingKey = reserveAllocationCompletedRoutingKey;
    }
}
