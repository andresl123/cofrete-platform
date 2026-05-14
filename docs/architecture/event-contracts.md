# Event Contracts

Events use JSON payloads, explicit canonical event type names, version numbers, idempotency keys, and correlation IDs for traceability.

## Canonical Names

The canonical MVP events are:

- `trip.recalculation.requested`
- `trip.finance.recalculated`
- `reserve.allocation.requested`
- `reserve.allocation.completed`
- `fuel-price.import.completed`
- `toll-data.import.completed`

Implementation tasks must use these names exactly. Legacy planning names are not valid contract names and must not be introduced unless a future migration compatibility issue explicitly scopes that work.

## Shared Envelope

Every event payload includes these fields:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | One canonical event name from this document. |
| `version` | integer | Yes | Contract version for this event payload. Current value is `1`. |
| `eventId` | string | Yes | Globally unique event ID for event-log tracing. |
| `idempotencyKey` | string | Yes | Stable key used by consumers to deduplicate domain effects. |
| `correlationId` | string | Yes | Trace ID from HTTP request, scheduled import, or worker command. |
| `producer` | string | Yes | Producing service name. |
| `occurredAt` or action timestamp | string | Yes | ISO-8601 UTC timestamp. Use the event-specific timestamp field where defined. |

Event rules:

- Producers publish only after the state change or import audit record that justifies the event is durable.
- Consumers must be idempotent by `idempotencyKey`; `eventId` is also stored for traceability.
- Consumers must reject unknown `eventType` values on typed handlers.
- Consumers must reject unsupported `version` values unless a compatibility adapter is explicitly implemented.
- Monetary values are decimal strings with explicit `currency`.
- Events must not contain secrets, raw document content, full credential material, or unnecessary CPF/CNPJ, RENAVAM, plate, RNTRC, bank, or payment identifiers.
- Events that reference official or external data must include source/freshness metadata or an import audit reference.
- Retryable failures may be retried with the same payload and same `idempotencyKey`.

## Delivery, Ordering, And Retry

The initial transport is RabbitMQ. Routing keys match `eventType`.

Delivery assumptions:

- Delivery is at least once.
- Global ordering is not guaranteed.
- Ordering is guaranteed only by consumer-side idempotency and by comparing domain timestamps or versioned snapshots.
- Consumers must tolerate duplicate, delayed, or out-of-order events.
- Producers should include enough identifiers for consumers to reload authoritative state from Core API or the database when the event is only a signal.

Retry behavior:

- Transient infrastructure failures should retry with bounded exponential backoff.
- Poison messages should move to a dead-letter queue with `eventId`, `eventType`, `idempotencyKey`, and `correlationId` visible in logs.
- Validation failures caused by unsupported event version or malformed payload are not blindly retryable.
- Downstream calculation failures should be traceable through `calculationTraceId` or the original `correlationId`.

## `trip.recalculation.requested`

Published by Core API when trip economics inputs change and Finance Worker should recalculate the trip.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `core-api` |
| Consumer | `finance-worker` |
| Routing key | `trip.recalculation.requested` |
| Idempotency key | `trip:{tripId}:recalculation:{inputRevision}` |
| Ordering | Multiple requests for a trip may arrive out of order. Consumer must use `inputRevision` and persisted trip state to avoid writing older results over newer snapshots. |
| Retry behavior | Retry transient worker or broker failures. Malformed payloads and missing trip references go to dead-letter with correlation ID. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `trip.recalculation.requested`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable trip/input-revision key. |
| `tripId` | string | Yes | Cofrete trip ID. |
| `driverId` | string | Yes | Cofrete driver ID that owns the trip. |
| `truckId` | string | Yes | Cofrete truck ID used for calculation inputs. |
| `inputRevision` | integer | Yes | Monotonic revision for trip finance inputs. |
| `reason` | string | Yes | Reason code for recalculation. |
| `correlationId` | string | Yes | Trace ID from source request or command. |
| `producer` | string | Yes | `core-api`. |
| `requestedAt` | string | Yes | ISO-8601 UTC timestamp. |

Valid `reason` values:

- `TRIP_CREATED`
- `TRIP_UPDATED`
- `EXPENSE_CREATED`
- `EXPENSE_UPDATED`
- `FUEL_ESTIMATE_UPDATED`
- `TOLL_CLASSIFICATION_UPDATED`
- `RESERVE_RULE_UPDATED`
- `RECEIVABLE_UPDATED`
- `IMPORT_DATA_REFRESHED`
- `MANUAL_RECALCULATION`

Example:

```json
{
  "eventType": "trip.recalculation.requested",
  "version": 1,
  "eventId": "evt_123",
  "idempotencyKey": "trip:trip_123:recalculation:7",
  "tripId": "trip_123",
  "driverId": "driver_123",
  "truckId": "truck_123",
  "inputRevision": 7,
  "reason": "EXPENSE_CREATED",
  "correlationId": "corr_123",
  "producer": "core-api",
  "requestedAt": "2026-05-11T12:00:00Z"
}
```

## `trip.finance.recalculated`

Published by Finance Worker after deterministic trip finance calculation completes.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `finance-worker` |
| Consumer | `core-api` |
| Routing key | `trip.finance.recalculated` |
| Idempotency key | `trip:{tripId}:finance-result:{inputRevision}:{calculationTraceId}` |
| Ordering | Core API must persist only the newest acceptable `inputRevision` for a trip and keep older results as audit records or discard them according to implementation policy. |
| Retry behavior | Retry transient persistence failures. Invalid monetary output is not retryable without a worker fix. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `trip.finance.recalculated`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable result key for the calculation output. |
| `tripId` | string | Yes | Cofrete trip ID. |
| `driverId` | string | Yes | Cofrete driver ID that owns the trip. |
| `inputRevision` | integer | Yes | Trip finance input revision used by the calculation. |
| `calculationTraceId` | string | Yes | Deterministic trace ID for audit and support. |
| `grossFreight` | string | Yes | Gross freight revenue. |
| `passThroughAmount` | string | Yes | Toll reimbursement, Vale-Pedagio, or other pass-through amount that is not profit. |
| `directTripCost` | string | Yes | Direct trip costs, including fuel and non-reimbursed tolls. |
| `requiredReserves` | string | Yes | Required reserve allocation. |
| `safePersonalWithdrawal` | string | Yes | Advisory amount safe for driver withdrawal. |
| `expectedProfit` | string | Yes | Advisory expected profit after costs/reserves. |
| `currency` | string | Yes | ISO currency, MVP value `BRL`. |
| `financialHealthImpact` | string | Yes | Finance status enum. |
| `sourceFreshness` | object | Yes | Fuel/toll/tax/compliance freshness summary used in calculation. |
| `correlationId` | string | Yes | Trace ID from original request chain. |
| `producer` | string | Yes | `finance-worker`. |
| `calculatedAt` | string | Yes | ISO-8601 UTC timestamp. |

Valid `financialHealthImpact` values:

- `GOOD`
- `ATTENTION`
- `RISK`
- `UNKNOWN`

Example:

```json
{
  "eventType": "trip.finance.recalculated",
  "version": 1,
  "eventId": "evt_124",
  "idempotencyKey": "trip:trip_123:finance-result:7:calc_123",
  "tripId": "trip_123",
  "driverId": "driver_123",
  "inputRevision": 7,
  "calculationTraceId": "calc_123",
  "grossFreight": "8000.00",
  "passThroughAmount": "385.70",
  "directTripCost": "4900.00",
  "requiredReserves": "1600.00",
  "safePersonalWithdrawal": "1100.00",
  "expectedProfit": "1100.00",
  "currency": "BRL",
  "financialHealthImpact": "GOOD",
  "sourceFreshness": {
    "fuel": "CURRENT",
    "toll": "CURRENT",
    "tax": "CURRENT",
    "compliance": "UNKNOWN"
  },
  "correlationId": "corr_123",
  "producer": "finance-worker",
  "calculatedAt": "2026-05-11T12:00:05Z"
}
```

Finance caveats:

- Toll reimbursement and Vale-Pedagio amounts are pass-through and must not be counted as profit.
- The event carries a calculation result, not an official tax, legal, insurance, or compliance decision.
- `calculationTraceId` must let support trace input assumptions and exact expected values in later deterministic tests.

## `reserve.allocation.requested`

Published when reserve allocation should be recalculated after freight payment, policy changes, or manual correction.

ROU-217 staged the worker-side allocation logic while keeping Core API as the temporary synchronous allocation owner. ROU-253 makes the async path authoritative: Core API publishes this request event with the active reserve rules used as deterministic inputs, Finance Worker calculates the allocation result, and Core API persists `reserve.allocation.completed` without double-crediting buckets.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `core-api` |
| Consumer | `finance-worker` |
| Routing key | `reserve.allocation.requested` |
| Idempotency key | `reserve:{allocationSubjectId}:{allocationRevision}` |
| Ordering | Requests for the same subject can arrive out of order. Consumer must use `allocationRevision` and current reserve rules. |
| Retry behavior | Retry transient worker or broker failures. Duplicate idempotency keys must not allocate twice. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `reserve.allocation.requested`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable reserve allocation key. |
| `accountId` | string | Yes | Cofrete account ID that owns the reserve wallet. |
| `allocationSubjectId` | string | Yes | Freight payment, manual correction, or rule-change subject ID. |
| `allocationRevision` | integer | Yes | Monotonic revision for allocation inputs. |
| `freightPaymentId` | string | Conditional | Required when allocation source is freight payment. |
| `tripId` | string | Conditional | Required when allocation relates to a trip. |
| `driverId` | string | Yes | Cofrete driver ID. |
| `grossAmount` | string | Yes | Amount available for allocation. |
| `passThroughAmount` | string | Yes | Amount excluded from reserves/profit when it is reimbursement or Vale-Pedagio. |
| `distanceKm` | string | No | Distance used by active `PER_KM` rules. |
| `currency` | string | Yes | ISO currency, MVP value `BRL`. |
| `reason` | string | Yes | Allocation trigger. |
| `rules` | array[object] | Yes | Active reserve rules with bucket, policy, rate/fixed/per-km values. |
| `correlationId` | string | Yes | Trace ID from source request or command. |
| `producer` | string | Yes | `core-api`. |
| `requestedAt` | string | Yes | ISO-8601 UTC timestamp. |

Valid `reason` values:

- `FREIGHT_PAYMENT_RECEIVED`
- `RESERVE_RULE_CHANGED`
- `MANUAL_CORRECTION`
- `RECEIVABLE_MARKED_PAID`

Example:

```json
{
  "eventType": "reserve.allocation.requested",
  "version": 1,
  "eventId": "evt_125",
  "idempotencyKey": "reserve:pay_123:3",
  "accountId": "acct_123",
  "allocationSubjectId": "pay_123",
  "allocationRevision": 3,
  "freightPaymentId": "pay_123",
  "tripId": "trip_123",
  "driverId": "driver_123",
  "grossAmount": "8000.00",
  "passThroughAmount": "385.70",
  "distanceKm": "1000.00",
  "currency": "BRL",
  "reason": "FREIGHT_PAYMENT_RECEIVED",
  "rules": [
    {
      "bucket": "MAINTENANCE",
      "policy": "PERCENT_OF_AMOUNT",
      "rate": "0.080000",
      "fixedAmount": null,
      "perKmAmount": null
    }
  ],
  "correlationId": "corr_123",
  "producer": "core-api",
  "requestedAt": "2026-05-11T12:00:10Z"
}
```

Reserve caveats:

- Reserve allocation events request virtual ledger allocation, not real payment movement.
- Pass-through toll or Vale-Pedagio amounts are excluded from profit treatment.
- Manual corrections must remain auditable through the allocation subject and revision.

## `reserve.allocation.completed`

Published by Finance Worker after deterministic reserve allocation completes.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `finance-worker` |
| Consumer | `core-api` |
| Routing key | `reserve.allocation.completed` |
| Idempotency key | Same stable key as the corresponding `reserve.allocation.requested` request. |
| Ordering | Core API ignores results older than the latest `allocationRevision` for the same allocation subject and never applies a completed allocation twice. |
| Retry behavior | Retry transient broker or Core API persistence failures with the same `idempotencyKey`. Duplicate deliveries must not create duplicate wallet credits or transactions. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `reserve.allocation.completed`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable key matching the request/allocation record. |
| `accountId` | string | Yes | Cofrete account ID that owns the reserve wallet. |
| `allocationSubjectId` | string | Yes | Freight payment, manual correction, or rule-change subject ID. |
| `allocationRevision` | integer | Yes | Monotonic revision used for calculation. |
| `driverId` | string | Yes | Cofrete driver ID. |
| `grossAmount` | string | Yes | Gross amount considered by the worker. |
| `passThroughAmount` | string | Yes | Pass-through amount excluded before allocation. |
| `allocatableAmount` | string | Yes | Gross minus pass-through amount. |
| `requiredReserveAmount` | string | Yes | Sum allocated to required reserve buckets. |
| `safePersonalWithdrawal` | string | Yes | Advisory safe withdrawal result. |
| `currency` | string | Yes | ISO currency, MVP value `BRL`. |
| `bucketAllocations` | object | Yes | Map of reserve bucket name to allocated decimal string. |
| `allocationTraceId` | string | Yes | Deterministic worker trace ID for audit/support. |
| `correlationId` | string | Yes | Trace ID from original request chain. |
| `producer` | string | Yes | `finance-worker`. |
| `allocatedAt` | string | Yes | ISO-8601 UTC timestamp. |

Example:

```json
{
  "eventType": "reserve.allocation.completed",
  "version": 1,
  "eventId": "evt_128",
  "idempotencyKey": "reserve:pay_123:3",
  "accountId": "acct_123",
  "allocationSubjectId": "pay_123",
  "allocationRevision": 3,
  "driverId": "driver_123",
  "grossAmount": "8000.00",
  "passThroughAmount": "385.70",
  "allocatableAmount": "7614.30",
  "requiredReserveAmount": "609.14",
  "safePersonalWithdrawal": "7005.16",
  "currency": "BRL",
  "bucketAllocations": {
    "MAINTENANCE": "609.14"
  },
  "allocationTraceId": "reserve_alloc_123",
  "correlationId": "corr_123",
  "producer": "finance-worker",
  "allocatedAt": "2026-05-11T12:00:12Z"
}
```

Reserve result caveats:

- The result is a virtual ledger allocation, not real payment movement.
- Core API persists the result idempotently so duplicate delivery cannot double-credit reserve wallets.
- Safe personal withdrawal remains advisory and excludes pass-through cash flow.

## `fuel-price.import.completed`

Published by Data Importer Worker after a fuel dataset import finishes and the import audit record is durable.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `data-importer-worker` |
| Consumer | `core-api`, optionally `finance-worker` as a recalculation signal |
| Routing key | `fuel-price.import.completed` |
| Idempotency key | `fuel-price:{source}:{sourcePeriodStart}:{sourcePeriodEnd}:{fileHash}` |
| Ordering | Imports may arrive late. Consumers must compare source period, retrieved time, and import audit status before marking latest prices current. |
| Retry behavior | Retry transient broker or consumer persistence failures. Parser failures should create failed import audit records and should not publish this completion event as successful. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `fuel-price.import.completed`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable source/period/file key. |
| `source` | string | Yes | Source name, MVP value `ANP`. |
| `dataset` | string | Yes | Dataset name, for example `diesel_prices`. |
| `fuelTypes` | array[string] | Yes | Imported fuel products. |
| `periodStart` | string | Yes | Source period start date. |
| `periodEnd` | string | Yes | Source period end date. |
| `retrievedAt` | string | Yes | Timestamp when source data was retrieved. |
| `recordsImported` | integer | Yes | Count of normalized records imported. |
| `freshnessStatus` | string | Yes | `CURRENT`, `STALE`, `FAILED`, or `UNKNOWN`. |
| `confidence` | string | Yes | Driver-facing confidence label. |
| `importAuditId` | string | Yes | Durable import audit record ID. |
| `fileHash` | string | No | Source file hash when available. |
| `correlationId` | string | Yes | Trace ID for scheduled import or manual command. |
| `producer` | string | Yes | `data-importer-worker`. |
| `completedAt` | string | Yes | ISO-8601 UTC timestamp. |

Example:

```json
{
  "eventType": "fuel-price.import.completed",
  "version": 1,
  "eventId": "evt_126",
  "idempotencyKey": "fuel-price:ANP:2026-05-03:2026-05-09:sha256-example",
  "source": "ANP",
  "dataset": "diesel_prices",
  "fuelTypes": ["DIESEL_S10"],
  "periodStart": "2026-05-03",
  "periodEnd": "2026-05-09",
  "retrievedAt": "2026-05-11T11:59:00Z",
  "recordsImported": 12345,
  "freshnessStatus": "CURRENT",
  "confidence": "official_weekly",
  "importAuditId": "import_123",
  "fileHash": "sha256-example",
  "correlationId": "corr_123",
  "producer": "data-importer-worker",
  "completedAt": "2026-05-11T12:00:20Z"
}
```

Fuel import caveats:

- ANP freshness must remain visible in fuel APIs and finance outputs.
- A successful import event means Cofrete imported a dataset; it does not certify current station-level price.
- Consumers may request trip recalculation for affected trips, but must not assume every older trip needs recalculation.

## `toll-data.import.completed`

Published by Data Importer Worker after toll data import or normalization completes and the import audit record is durable.

Contract metadata:

| Property | Value |
|---|---|
| Producer | `data-importer-worker` |
| Consumer | `core-api`, optionally `finance-worker` as a recalculation signal |
| Routing key | `toll-data.import.completed` |
| Idempotency key | `toll-data:{source}:{sourcePeriodStart}:{sourcePeriodEnd}:{fileHash}` |
| Ordering | Imports may arrive late or cover overlapping source periods. Consumers must compare source period, effective dates, and import audit status. |
| Retry behavior | Retry transient broker or consumer persistence failures. Parser failures should create failed import audit records and should not publish this completion event as successful. |

Schema:

| Field | Type | Required | Semantics |
|---|---|---|---|
| `eventType` | string | Yes | Must be `toll-data.import.completed`. |
| `version` | integer | Yes | Must be `1`. |
| `eventId` | string | Yes | Unique event ID. |
| `idempotencyKey` | string | Yes | Stable source/period/file key. |
| `source` | string | Yes | Source name, for example `ANTT_DADOS_ABERTOS`. |
| `dataset` | string | Yes | Dataset name, for example `toll_plazas_and_tariffs`. |
| `periodStart` | string | No | Source period start date when the dataset provides one. |
| `periodEnd` | string | No | Source period end date when the dataset provides one. |
| `retrievedAt` | string | Yes | Timestamp when source data was retrieved. |
| `recordsImported` | integer | Yes | Count of normalized records imported. |
| `formats` | array[string] | Yes | Imported source formats. |
| `freshnessStatus` | string | Yes | `CURRENT`, `STALE`, `FAILED`, or `UNKNOWN`. |
| `confidence` | string | Yes | Driver-facing confidence label. |
| `importAuditId` | string | Yes | Durable import audit record ID. |
| `fileHash` | string | No | Source file hash when available. |
| `correlationId` | string | Yes | Trace ID for scheduled import or manual command. |
| `producer` | string | Yes | `data-importer-worker`. |
| `completedAt` | string | Yes | ISO-8601 UTC timestamp. |

Example:

```json
{
  "eventType": "toll-data.import.completed",
  "version": 1,
  "eventId": "evt_127",
  "idempotencyKey": "toll-data:ANTT_DADOS_ABERTOS:2026-05-01:2026-05-31:sha256-example",
  "source": "ANTT_DADOS_ABERTOS",
  "dataset": "toll_plazas_and_tariffs",
  "periodStart": "2026-05-01",
  "periodEnd": "2026-05-31",
  "retrievedAt": "2026-05-11T11:58:00Z",
  "recordsImported": 500,
  "formats": ["CSV", "JSON"],
  "freshnessStatus": "CURRENT",
  "confidence": "official_or_open_dataset",
  "importAuditId": "import_124",
  "fileHash": "sha256-example",
  "correlationId": "corr_123",
  "producer": "data-importer-worker",
  "completedAt": "2026-05-11T12:00:30Z"
}
```

Toll import caveats:

- Imported toll data supports estimates; it is not a payment proof.
- Vale-Pedagio and toll reimbursement remain pass-through money.
- Stale, failed, or unknown freshness must degrade user-facing confidence instead of pretending precision.

## Consumer Validation Checklist

Consumers must validate:

- `eventType` equals the handler's canonical name.
- `version` is supported.
- `eventId`, `idempotencyKey`, and `correlationId` are present.
- Required domain IDs are present.
- Decimal strings are parseable and non-negative where money cannot be negative.
- `currency` is present for monetary payloads.
- Source/freshness metadata is present for import events and source-backed finance outputs.
- The event is authorized for the producing service identity at the broker or application boundary.

## Producer Checklist

Producers must:

- Publish with routing key equal to `eventType`.
- Set `producer` to the service name.
- Reuse the same `idempotencyKey` for retries of the same domain effect.
- Create durable state or import audit records before publishing.
- Include `correlationId` in logs, message headers when supported, and payload.
- Avoid older event spellings in configuration, tests, fixtures, and docs except when explicitly documenting why they are invalid.
