package com.cofrete.financeworker.reserve;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ReserveAllocationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ReserveAllocationInputSnapshotProvider reserveAllocationInputSnapshotProvider() {
        return new UnavailableReserveAllocationInputSnapshotProvider();
    }
}
