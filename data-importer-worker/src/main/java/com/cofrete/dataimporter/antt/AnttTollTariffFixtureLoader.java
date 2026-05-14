package com.cofrete.dataimporter.antt;

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
public class AnttTollTariffFixtureLoader {

    private static final String HEADER = "plaza_external_id,plaza_name,highway,state,municipality,km_marker,direction,latitude,longitude,vehicle_category,axle_count,amount_brl,currency,effective_start,effective_end,source_url,freshness_status,confidence";

    private final Resource fixture;

    public AnttTollTariffFixtureLoader(
        @Value("${cofrete.data-importer.antt.fixture:classpath:fixtures/antt-toll-plazas-tariffs-synthetic.csv}") Resource fixture
    ) {
        this.fixture = fixture;
    }

    public List<AnttTollTariffFixtureRecord> load() throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(fixture.getInputStream(), StandardCharsets.UTF_8)
        )) {
            List<String> lines = reader.lines()
                .filter(line -> !line.isBlank())
                .toList();

            if (lines.isEmpty() || !HEADER.equals(lines.getFirst())) {
                throw new IllegalStateException("ANTT toll fixture header is invalid");
            }

            return lines.stream()
                .skip(1)
                .map(this::toRecord)
                .toList();
        }
    }

    private AnttTollTariffFixtureRecord toRecord(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length != 18) {
            throw new IllegalStateException("ANTT toll fixture row must have 18 columns");
        }

        return new AnttTollTariffFixtureRecord(
            columns[0],
            columns[1],
            columns[2],
            columns[3],
            columns[4],
            new BigDecimal(columns[5]),
            columns[6],
            new BigDecimal(columns[7]),
            new BigDecimal(columns[8]),
            columns[9],
            Integer.parseInt(columns[10]),
            new BigDecimal(columns[11]),
            columns[12],
            LocalDate.parse(columns[13]),
            LocalDate.parse(columns[14]),
            columns[15],
            FreshnessStatus.valueOf(columns[16]),
            columns[17]
        );
    }
}
