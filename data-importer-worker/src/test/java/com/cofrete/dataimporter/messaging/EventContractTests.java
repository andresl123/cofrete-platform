package com.cofrete.dataimporter.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventContractTests {

    @Test
    void usesCanonicalImportEventNamesFromArchitectureDocs() throws IOException {
        List<String> canonicalEventNames = List.of(
            DataImportEventNames.FUEL_PRICE_IMPORT_COMPLETED,
            DataImportEventNames.TOLL_DATA_IMPORT_COMPLETED
        );

        String eventContracts = Files.readString(Path.of("..", "docs", "architecture", "event-contracts.md"));

        assertThat(canonicalEventNames).containsExactly(
            "fuel-price.import.completed",
            "toll-data.import.completed"
        );
        assertThat(canonicalEventNames)
            .allSatisfy(eventName -> assertThat(eventContracts).contains("`" + eventName + "`"));
        assertThat(canonicalEventNames).doesNotContain("fuel-prices.import.completed");
    }
}
