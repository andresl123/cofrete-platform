package com.cofrete.dataimporter.anp;

import static org.assertj.core.api.Assertions.assertThat;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AnpDieselPriceFixtureLoaderTests {

    private final AnpDieselPriceFixtureLoader loader = new AnpDieselPriceFixtureLoader(
        new ClassPathResource("fixtures/anp-diesel-prices-synthetic.csv")
    );

    @Test
    void loadsSyntheticAnpDieselFixtureWithFreshnessAndConfidence() throws IOException {
        var records = loader.load();

        assertThat(records).hasSize(3);
        assertThat(records)
            .allSatisfy(record -> {
                assertThat(record.freshnessStatus()).isEqualTo(FreshnessStatus.CURRENT);
                assertThat(record.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
                assertThat(record.pricePerLiterBrl()).isGreaterThan(BigDecimal.ZERO);
            });
        assertThat(records).extracting(AnpDieselPriceFixtureRecord::fuelType)
            .contains("DIESEL_S10", "DIESEL_S500");
    }
}
