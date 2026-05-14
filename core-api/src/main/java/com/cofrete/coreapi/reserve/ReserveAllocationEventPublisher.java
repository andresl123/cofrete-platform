package com.cofrete.coreapi.reserve;

interface ReserveAllocationEventPublisher {

    void publish(ReserveAllocationRequestedEvent event);
}
