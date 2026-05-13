package com.cofrete.coreapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.cofrete.coreapi.config.ApiRoutingConventions;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModuleBoundaryTests {

    private static final List<Class<?>> MODULE_MARKERS = List.of(
        com.cofrete.coreapi.profile.ProfileModule.class,
        com.cofrete.coreapi.trip.TripModule.class,
        com.cofrete.coreapi.finance.FinanceModule.class,
        com.cofrete.coreapi.reserve.ReserveModule.class,
        com.cofrete.coreapi.compliance.ComplianceModule.class,
        com.cofrete.coreapi.receivables.ReceivablesModule.class,
        com.cofrete.coreapi.imports.ImportsModule.class,
        com.cofrete.coreapi.auth.AuthModule.class,
        com.cofrete.coreapi.audit.AuditModule.class
    );

    @Test
    void reservesDocumentedInternalModuleBoundaries() {
        assertThat(MODULE_MARKERS)
            .extracting(Class::getPackageName)
            .containsExactly(
                "com.cofrete.coreapi.profile",
                "com.cofrete.coreapi.trip",
                "com.cofrete.coreapi.finance",
                "com.cofrete.coreapi.reserve",
                "com.cofrete.coreapi.compliance",
                "com.cofrete.coreapi.receivables",
                "com.cofrete.coreapi.imports",
                "com.cofrete.coreapi.auth",
                "com.cofrete.coreapi.audit"
            );
    }

    @Test
    void reservesApiPrefixForContractedRoutes() {
        assertThat(ApiRoutingConventions.API_PREFIX).isEqualTo("/api");
    }
}
