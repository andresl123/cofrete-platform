package com.cofrete.financeworker.recalculation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TripFinanceRecalculationConfiguration {

    @Bean
    @ConditionalOnMissingBean(TripFinanceInputSnapshotProvider.class)
    TripFinanceInputSnapshotProvider tripFinanceInputSnapshotProvider() {
        return new UnavailableTripFinanceInputSnapshotProvider();
    }
}
