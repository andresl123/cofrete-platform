package com.cofrete.dataimporter.scheduling;

import com.cofrete.dataimporter.anp.AnpDieselPriceImportJob;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ImportScheduler {

    private final AnpDieselPriceImportJob anpDieselPriceImportJob;
    private final boolean enabled;

    public ImportScheduler(
        AnpDieselPriceImportJob anpDieselPriceImportJob,
        @Value("${cofrete.data-importer.scheduler.enabled:false}") boolean enabled
    ) {
        this.anpDieselPriceImportJob = anpDieselPriceImportJob;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${cofrete.data-importer.scheduler.anp-diesel-cron:0 0 3 * * *}")
    void runAnpDieselFixtureImport() throws IOException {
        if (!enabled) {
            return;
        }

        anpDieselPriceImportJob.publishSyntheticFixtureCompleted("scheduler-anp-diesel");
    }
}
