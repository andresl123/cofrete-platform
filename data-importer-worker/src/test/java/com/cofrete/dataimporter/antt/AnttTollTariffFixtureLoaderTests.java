package com.cofrete.dataimporter.antt;

import static org.assertj.core.api.Assertions.assertThat;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AnttTollTariffFixtureLoaderTests {

    private final AnttTollTariffFixtureLoader loader = new AnttTollTariffFixtureLoader(
        new ClassPathResource("fixtures/antt-toll-plazas-tariffs-synthetic.csv")
    );

    @Test
    void loadsSyntheticAnttTollFixtureWithSourceAndTariffs() throws IOException {
        var records = loader.load();

        assertThat(records).hasSize(2);
        assertThat(records)
            .allSatisfy(record -> {
                assertThat(record.sourceUrl()).isEqualTo("https://dados.antt.gov.br/dataset/praca-de-pedagio");
                assertThat(record.freshnessStatus()).isEqualTo(FreshnessStatus.CURRENT);
                assertThat(record.confidence()).isEqualTo("SYNTHETIC_FIXTURE");
                assertThat(record.amountBrl()).isGreaterThan(BigDecimal.ZERO);
                assertThat(record.currency()).isEqualTo("BRL");
                assertThat(record.axleCount()).isEqualTo(6);
            });
        assertThat(records).extracting(AnttTollTariffFixtureRecord::state)
            .containsExactly("GO", "SP");
    }
}
