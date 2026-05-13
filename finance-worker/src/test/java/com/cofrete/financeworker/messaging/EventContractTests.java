package com.cofrete.financeworker.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventContractTests {

    @Test
    void usesCanonicalFinanceEventNamesFromArchitectureDocs() throws IOException {
        List<String> canonicalEventNames = List.of(
            FinanceEventNames.TRIP_RECALCULATION_REQUESTED,
            FinanceEventNames.RESERVE_ALLOCATION_REQUESTED,
            FinanceEventNames.TRIP_FINANCE_RECALCULATED
        );

        String eventContracts = Files.readString(Path.of("..", "docs", "architecture", "event-contracts.md"));

        assertThat(canonicalEventNames).containsExactly(
            "trip.recalculation.requested",
            "reserve.allocation.requested",
            "trip.finance.recalculated"
        );
        assertThat(canonicalEventNames)
            .allSatisfy(eventName -> assertThat(eventContracts).contains("`" + eventName + "`"));
        assertThat(canonicalEventNames).doesNotContain("trip.finance.recalculate.requested");
    }
}
