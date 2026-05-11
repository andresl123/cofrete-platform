# Event Contracts

Events use JSON payloads, explicit event type names, idempotency keys where needed, and correlation IDs for traceability.

## `trip.finance.recalculate.requested`

Published by Core API when trip economics change.

```json
{
  "eventType": "trip.finance.recalculate.requested",
  "eventId": "evt_123",
  "tripId": "trip_123",
  "driverId": "driver_123",
  "reason": "EXPENSE_CREATED",
  "correlationId": "corr_123",
  "occurredAt": "2026-05-11T12:00:00Z"
}
```

## `trip.finance.recalculated`

Published by Finance Worker after deterministic calculation.

```json
{
  "eventType": "trip.finance.recalculated",
  "eventId": "evt_124",
  "tripId": "trip_123",
  "grossFreight": "8000.00",
  "directTripCost": "4900.00",
  "requiredReserves": "1600.00",
  "safePersonalWithdrawal": "1100.00",
  "currency": "BRL",
  "financialHealthImpact": "GOOD",
  "correlationId": "corr_123",
  "occurredAt": "2026-05-11T12:00:05Z"
}
```

## `reserve.allocation.requested`

Published when reserve allocation should be recalculated after freight payment, policy changes, or manual correction.

## `fuel-prices.import.completed`

Published by Data Importer Worker after a fuel dataset import finishes.

Required metadata:

- source name.
- source URL or dataset ID.
- imported period.
- row count.
- freshness status.
- import audit ID.

## `toll-data.import.completed`

Published by Data Importer Worker after toll data import or normalization completes.

## Rules

- Consumers must be idempotent by `eventId` or domain-specific idempotency key.
- Monetary values are decimal strings with explicit currency.
- Events must not contain secrets or raw document content.
