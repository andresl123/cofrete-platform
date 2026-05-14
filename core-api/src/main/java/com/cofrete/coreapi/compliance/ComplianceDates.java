package com.cofrete.coreapi.compliance;

import java.time.Clock;
import java.time.LocalDate;

final class ComplianceDates {

    static final int EXPIRING_SOON_DAYS = 60;

    private ComplianceDates() {
    }

    static ComplianceItemStatus statusFor(LocalDate expiresOn) {
        return statusFor(expiresOn, Clock.systemUTC());
    }

    static ComplianceItemStatus statusFor(LocalDate expiresOn, Clock clock) {
        if (expiresOn == null) {
            return ComplianceItemStatus.UNKNOWN;
        }
        var today = LocalDate.now(clock);
        if (expiresOn.isBefore(today)) {
            return ComplianceItemStatus.EXPIRED;
        }
        if (!expiresOn.isAfter(today.plusDays(EXPIRING_SOON_DAYS))) {
            return ComplianceItemStatus.EXPIRING_SOON;
        }
        return ComplianceItemStatus.CURRENT;
    }
}
