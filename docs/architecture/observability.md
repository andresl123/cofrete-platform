# Observability

Each service should expose health and readiness endpoints once scaffolded.

## Required Signals

- Structured logs.
- Correlation IDs across API requests and events.
- Import audit records.
- Finance calculation audit records.
- Retry and dead-letter visibility for workers.
- Dataset freshness indicators.

## High-Value Events

- Finance recalculation failed.
- Fuel price dataset stale.
- Toll dataset stale.
- Data import failed.
- Reserve allocation created.
- Document expiration alert generated.
- Compliance source status unknown.

## Log Rules

- Log identifiers and correlation IDs, not raw secrets or document content.
- Include calculation trace IDs for financial results.
- Include import audit IDs for imported datasets.
