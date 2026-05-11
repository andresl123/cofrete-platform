# Event Contracts

Events use JSON payloads, explicit event type names, idempotency keys where needed, and correlation IDs for traceability.

## Naming Decision

The master blueprint names the trip request event `trip.recalculation.requested` and the fuel import event `fuel-price.import.completed`. Those names are canonical going forward. Earlier planning used `trip.finance.recalculate.requested` and `fuel-prices.import.completed`; implementations should not introduce those older names unless a migration compatibility issue explicitly requires them.

## `trip.recalculation.requested`

Published by Core API when trip economics change.

```json
{
  "eventType": "trip.recalculation.requested",
  "version": 1,
  "eventId": "evt_123",
  "tripId": "TRIP-001",
  "driverId": "DRIVER-001",
  "truckId": "TRUCK-001",
  "reason": "EXPENSE_CREATED",
  "correlationId": "corr_123",
  "requestedAt": "2026-05-11T12:00:00Z"
}
```

## `trip.finance.recalculated`

Published by Finance Worker after deterministic calculation.

```json
{
  "eventType": "trip.finance.recalculated",
  "version": 1,
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

```json
{
  "eventType": "reserve.allocation.requested",
  "version": 1,
  "eventId": "evt_125",
  "freightPaymentId": "PAY-001",
  "tripId": "TRIP-001",
  "grossAmount": "8000.00",
  "currency": "BRL",
  "correlationId": "corr_123",
  "requestedAt": "2026-05-11T12:00:10Z"
}
```

## `fuel-price.import.completed`

Published by Data Importer Worker after a fuel dataset import finishes.

```json
{
  "eventType": "fuel-price.import.completed",
  "version": 1,
  "eventId": "evt_126",
  "source": "ANP",
  "fuelTypes": ["DIESEL_S10"],
  "periodStart": "2026-05-03",
  "periodEnd": "2026-05-09",
  "recordsImported": 12345,
  "freshnessStatus": "CURRENT",
  "importAuditId": "import_123",
  "correlationId": "corr_123",
  "completedAt": "2026-05-11T12:00:20Z"
}
```

## `toll-data.import.completed`

Published by Data Importer Worker after toll data import or normalization completes.

```json
{
  "eventType": "toll-data.import.completed",
  "version": 1,
  "eventId": "evt_127",
  "source": "ANTT_DADOS_ABERTOS",
  "recordsImported": 500,
  "formats": ["CSV", "JSON"],
  "freshnessStatus": "CURRENT",
  "importAuditId": "import_124",
  "correlationId": "corr_123",
  "completedAt": "2026-05-11T12:00:30Z"
}
```

## Rules

- Consumers must be idempotent by `eventId` or domain-specific idempotency key.
- Monetary values are decimal strings with explicit currency.
- Events must not contain secrets or raw document content.
- Event payloads must include `version`, `correlationId`, and a timestamp field named for the event action, such as `requestedAt`, `completedAt`, or `occurredAt`.
