package com.cofrete.dataimporter.anp;

import com.cofrete.dataimporter.imports.FreshnessStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AnpDieselPriceFixtureLoader {

    private final Resource fixture;

    public AnpDieselPriceFixtureLoader(
        @Value("${cofrete.data-importer.anp.fixture:classpath:fixtures/anp-diesel-prices-synthetic.csv}") Resource fixture
    ) {
        this.fixture = fixture;
    }

    public List<AnpDieselPriceFixtureRecord> load() throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(fixture.getInputStream(), StandardCharsets.UTF_8)
        )) {
            List<String> lines = reader.lines()
                .filter(line -> !line.isBlank())
                .toList();

            if (lines.isEmpty() || !"fuel_type,state,price_per_liter_brl,period_start,period_end,freshness_status,confidence".equals(lines.getFirst())) {
                throw new IllegalStateException("ANP diesel fixture header is invalid");
            }

            return lines.stream()
                .skip(1)
                .map(this::toRecord)
                .toList();
        }
    }

    private AnpDieselPriceFixtureRecord toRecord(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length != 7) {
            throw new IllegalStateException("ANP diesel fixture row must have 7 columns");
        }

        return new AnpDieselPriceFixtureRecord(
            columns[0],
            columns[1],
            new BigDecimal(columns[2]),
            LocalDate.parse(columns[3]),
            LocalDate.parse(columns[4]),
            FreshnessStatus.valueOf(columns[5]),
            columns[6]
        );
    }
}
