# Incident Response Runbook

## Incident Types

- Wrong finance calculation.
- Stale or wrong imported data.
- Document or private data exposure.
- Authentication/authorization failure.
- Compliance wording that may mislead users.

## First Response

1. Preserve logs and correlation IDs.
2. Identify affected users, trips, calculations, or imports.
3. Stop unsafe automation if needed.
4. Communicate impact in plain language.
5. Create follow-up issues for code, tests, and docs.

## Finance Incident Rule

If safe withdrawal, profit, reserve, toll, or fuel math is wrong, treat it as high risk. Add or update deterministic fixture tests before closing the fix.
