# Data Import Failure Runbook

## Applies To

- ANP diesel price import failures.
- Future ANTT toll dataset import failures.
- Fixture parsing failures.
- Dataset freshness alerts.

## Triage

1. Check import audit record.
2. Check source URL and dataset timestamp.
3. Check parser errors and row counts.
4. Check retry and dead-letter state.
5. Mark source confidence as stale or unknown if needed.

## User Impact

When official data is stale or unavailable, Cofrete should show source confidence and allow manual driver price input instead of hiding the issue.

## Recovery

- Re-run the importer after source or parser fix.
- Keep previous known-good data with visible freshness.
- Document source changes in `docs/compliance/official-source-register.md` when needed.
