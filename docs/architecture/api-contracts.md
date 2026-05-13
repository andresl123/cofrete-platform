# API Contracts

These contracts are the Core API implementation target for the MVP. They are versioned by this canonical markdown document until OpenAPI generation is introduced.

## Contract Versioning

- Current contract version: `v1`.
- Public paths use the `/api` prefix.
- Breaking field, enum, authorization, or error-shape changes require a contract update before implementation.
- Additive optional fields are allowed when older clients can ignore them safely.
- All timestamps are ISO-8601 UTC strings.
- Monetary values are decimal strings with explicit `currency`.
- Distances use kilometers and decimal strings when precision matters.
- IDs shown here are opaque application IDs unless a field explicitly identifies an official source identifier.

## Endpoint Index

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET /api/auth/me
POST /api/drivers
GET /api/drivers/me
PATCH /api/drivers/me
POST /api/trucks
GET /api/trucks
GET /api/trucks/{truckId}
PUT /api/trucks/{truckId}
POST /api/tax-profile
GET /api/tax-profile
POST /api/trips
GET /api/trips/{tripId}
POST /api/trips/{tripId}/profitability-estimate
GET /api/trips/{tripId}/profitability-snapshot
POST /api/trips/{tripId}/acceptance-decision
POST /api/expenses
GET /api/expenses?from=&to=&category=
POST /api/reserve-rules
GET /api/reserve-wallets
POST /api/reserve-allocations
GET /api/financial-health-score
GET /api/fuel-prices/latest?fuel=DIESEL_S10&state=GO&city=GOIANIA
GET /api/fuel-prices/history?fuel=DIESEL_S10&state=GO&city=GOIANIA
POST /api/fuel-prices/driver-report
GET /api/trucks/{truckId}/consumption-profile
POST /api/trips/{tripId}/fuel-estimate
POST /api/toll-estimates
GET /api/trips/{tripId}/tolls
POST /api/trips/{tripId}/tolls/manual-payment
POST /api/trips/{tripId}/vale-pedagio
GET /api/toll-data/import-status
GET /api/compliance/profile
PUT /api/compliance/rntrc
POST /api/compliance/rntrc/check-public-status
POST /api/compliance/insurance-policies
GET /api/compliance/insurance-policies
GET /api/compliance/score
GET /api/compliance/calendar
GET /api/documents
POST /api/documents
POST /api/customers
GET /api/customers/{customerId}/profitability
POST /api/receivables
GET /api/receivables?status=overdue
PUT /api/receivables/{receivableId}/mark-paid
```

## Advisory Boundary

Cofrete stores authoritative app records only for data entered by the driver, created by Cofrete, or imported and audited by Cofrete. Profitability, reserves, safe withdrawal, fuel, toll, tax, insurance, RNTRC, CIOT, freight-floor, IPVA, licensing, and waiting-time data are advisory unless the response includes a documented official source, source period, and freshness timestamp.

Responses that use official or external data must include source metadata where applicable:

```json
{
  "source": "ANP",
  "sourceType": "official_dataset",
  "sourceUrl": "https://www.gov.br/anp",
  "sourcePeriodStart": "2026-05-03",
  "sourcePeriodEnd": "2026-05-09",
  "retrievedAt": "2026-05-11T12:00:00Z",
  "freshnessStatus": "CURRENT",
  "confidence": "official_weekly",
  "importAuditId": "import_123"
}
```

Freshness values:

- `CURRENT`
- `STALE`
- `FAILED`
- `UNKNOWN`

Official-source caveats:

- ANP diesel prices must show source period, retrieval time, confidence, and stale/unknown state when current data is unavailable.
- Toll estimates and Vale-Pedagio records are pass-through or reimbursement data and must not inflate profit or safe withdrawal.
- Compliance APIs must use advisory wording and official-channel links; Cofrete must not imply it updates RNTRC, Receita Federal, ANTT, DETRAN, SEFAZ, SUSEP, insurer, or gov.br records.
- Tax and insurance outputs are planning metadata unless backed by a documented source and review timestamp.

## Auth And Sessions

Auth endpoints are owned by Core API. Cofrete app login is separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, or other official systems.

All non-auth product APIs require an authenticated Cofrete principal unless an implementation issue explicitly documents a public endpoint.

### Principal Model

```json
{
  "id": "user_123",
  "type": "DRIVER",
  "accountId": "acct_123",
  "companyId": null,
  "roles": ["DRIVER"]
}
```

Valid user role values:

- `DRIVER`
- `COMPANY_ADMIN` reserved for post-MVP company mode; not active or assignable during the single-driver MVP
- `SUPPORT`
- `PLATFORM_ADMIN`

Valid internal service role values:

- `SERVICE_FINANCE_WORKER`
- `SERVICE_DATA_IMPORTER_WORKER`

Tokens and session metadata may include principal ID, principal type, roles, account ID, company ID, issued-at time, expiry, and token ID. Tokens must not include CPF/CNPJ, truck plate, RENAVAM, RNTRC, document contents, freight values, receivable values, bank/payment data, or compliance document data.

### Auth Endpoints

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/auth/register` | Create Cofrete application identity. | Email, password, display name, client type. | Principal. | `VALIDATION_ERROR`, `CONFLICT`, `RATE_LIMITED` |
| `POST` | `/api/auth/login` | Authenticate Cofrete credentials and create refresh session. | Email, password, client type. | Access token, refresh token or cookie, principal. | `UNAUTHENTICATED`, `RATE_LIMITED` |
| `POST` | `/api/auth/refresh` | Rotate or validate refresh session. | Refresh token or secure cookie. | New access token, optional rotated refresh token, principal. | `UNAUTHENTICATED`, `SESSION_REVOKED` |
| `POST` | `/api/auth/logout` | Revoke active refresh session. | Refresh token or secure cookie. | Revocation status. | `UNAUTHENTICATED` |
| `GET` | `/api/auth/me` | Return authenticated principal and linked profile references. | None. | Principal, driver profile reference, company reference. | `UNAUTHENTICATED` |

Example login request:

```json
{
  "email": "driver@example.com",
  "password": "example-password",
  "clientType": "MOBILE"
}
```

Example login response:

```json
{
  "accessToken": "jwt_or_opaque_access_token",
  "tokenType": "Bearer",
  "expiresInSeconds": 900,
  "refreshToken": "refresh_token_returned_only_when_client_type_allows_it",
  "principal": {
    "id": "user_123",
    "type": "DRIVER",
    "accountId": "acct_123",
    "companyId": null,
    "roles": ["DRIVER"]
  }
}
```

Auth rules:

- MVP self-registration creates a `DRIVER` principal by default.
- `COMPANY_ADMIN` assignment must remain disabled until a future company-mode issue implements that workflow.
- `SUPPORT` and `PLATFORM_ADMIN` assignment must be administrative and auditable.
- Access tokens must be short-lived.
- Refresh tokens or refresh sessions must be revocable.
- Mobile clients should store secrets only in platform secure storage.
- Web admin should prefer secure, HTTP-only, SameSite cookies when deployment shape allows it.
- Service-to-service credentials are separate from user login sessions.
- Workers authenticate as internal services with least privilege.

## Driver, Truck, And Tax Profile

These APIs own app-maintained driver identity metadata, truck metadata, and tax-planning profile data. They do not write official government records.

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/drivers` | Create driver profile for the authenticated principal. | Name, contact, state, CPF/CNPJ metadata, RNTRC metadata. | `driver` resource. | `VALIDATION_ERROR`, `CONFLICT`, `FORBIDDEN` |
| `GET` | `/api/drivers/me` | Return current driver's profile. | None. | `driver` resource with source/advisory metadata. | `UNAUTHENTICATED`, `NOT_FOUND` |
| `PATCH` | `/api/drivers/me` | Update app-maintained driver profile fields. | Partial editable driver fields. | Updated `driver` resource. | `VALIDATION_ERROR`, `FORBIDDEN` |
| `POST` | `/api/trucks` | Register a truck owned or operated by the driver. | Plate, RENAVAM metadata, axle count, category, fuel type. | `truck` resource. | `VALIDATION_ERROR`, `CONFLICT` |
| `GET` | `/api/trucks` | List driver trucks. | Optional `active=true`. | `trucks[]`. | `UNAUTHENTICATED` |
| `GET` | `/api/trucks/{truckId}` | Return truck metadata. | Path `truckId`. | `truck` resource. | `NOT_FOUND`, `FORBIDDEN` |
| `PUT` | `/api/trucks/{truckId}` | Replace editable truck metadata. | Full editable truck fields. | Updated `truck` resource. | `VALIDATION_ERROR`, `NOT_FOUND`, `FORBIDDEN` |
| `POST` | `/api/tax-profile` | Create or replace active tax-planning profile. | Regime, state, year, source/review metadata when known. | `taxProfile` resource. | `VALIDATION_ERROR`, `CONFLICT` |
| `GET` | `/api/tax-profile` | Return active tax-planning profile. | Optional `year`. | `taxProfile` resource. | `NOT_FOUND` |

Example truck request:

```json
{
  "plate": "ABC1D23",
  "renavamLast4": "1234",
  "state": "GO",
  "axleCount": 6,
  "vehicleType": "TRUCK",
  "fuelType": "DIESEL_S10",
  "active": true
}
```

Example tax profile response:

```json
{
  "taxProfile": {
    "id": "tax_123",
    "driverId": "driver_123",
    "regime": "MEI_CAMINHONEIRO",
    "planningYear": 2026,
    "annualGrossLimit": "251600.00",
    "currency": "BRL",
    "source": "Receita Federal",
    "sourceType": "official_guidance",
    "reviewedAt": "2026-05-10T10:00:00Z",
    "freshnessStatus": "CURRENT",
    "advisoryText": "Tax values are planning metadata. Confirm obligations with Receita Federal or an accountant."
  }
}
```

Field semantics:

- CPF/CNPJ, RENAVAM, RNTRC, plate, and document identifiers should be stored with minimum necessary exposure and masked in normal responses when full value is not needed.
- `TaxProfile` reflects planning assumptions. It is not tax filing.
- Official status fields require source URL or source name plus `reviewedAt` or `retrievedAt`.

## Freight And Trip Decision

The MVP starts from the driver workflow: "Should I accept this freight/trip?" Freight may become a separate aggregate later, but trip entry owns the first public contract.

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/trips` | Create a trip/freight decision draft. | Route, dates, truck, gross freight, payment timing, load details. | `trip` resource. | `VALIDATION_ERROR`, `NOT_FOUND` |
| `GET` | `/api/trips/{tripId}` | Return trip and current decision state. | Path `tripId`. | `trip` resource with latest snapshot links. | `NOT_FOUND`, `FORBIDDEN` |
| `POST` | `/api/trips/{tripId}/profitability-estimate` | Request synchronous estimate from provided or persisted inputs. | Cost assumptions, fuel/toll overrides, reserve policy overrides. | Estimate with trace ID and advisory caveats. | `VALIDATION_ERROR`, `NOT_FOUND`, `CALCULATION_UNAVAILABLE` |
| `GET` | `/api/trips/{tripId}/profitability-snapshot` | Return latest persisted finance-worker snapshot. | Path `tripId`. | Snapshot and calculation trace. | `NOT_FOUND`, `SNAPSHOT_PENDING` |
| `POST` | `/api/trips/{tripId}/acceptance-decision` | Record accept, reject, or renegotiate decision. | Decision, reason codes, note. | Decision record. | `VALIDATION_ERROR`, `NOT_FOUND`, `CONFLICT` |

Example trip creation request:

```json
{
  "truckId": "truck_123",
  "origin": {
    "city": "Goiania",
    "state": "GO"
  },
  "destination": {
    "city": "Sao Paulo",
    "state": "SP"
  },
  "loadedKm": "920.00",
  "emptyKm": "80.00",
  "grossFreight": "8000.00",
  "currency": "BRL",
  "advanceAmount": "3000.00",
  "balanceDueDays": 21,
  "cargoDescription": "general cargo",
  "expectedPickupAt": "2026-05-20T12:00:00Z"
}
```

Example profitability estimate response:

```json
{
  "tripId": "trip_123",
  "calculationTraceId": "calc_123",
  "grossFreight": "8000.00",
  "passThroughAmount": "385.70",
  "directTripCost": "4900.00",
  "requiredReserves": "1600.00",
  "safePersonalWithdrawal": "1100.00",
  "expectedProfit": "1100.00",
  "marginPercent": "13.75",
  "currency": "BRL",
  "financialHealthStatus": "GOOD",
  "recommendation": "ACCEPT",
  "caveats": [
    "Toll reimbursement and Vale-Pedagio are tracked as pass-through amounts, not profit.",
    "Fuel estimate uses ANP weekly data and truck consumption assumptions."
  ],
  "sourceMetadata": [
    {
      "area": "fuel",
      "source": "ANP",
      "sourceType": "official_dataset",
      "sourcePeriodStart": "2026-05-03",
      "sourcePeriodEnd": "2026-05-09",
      "retrievedAt": "2026-05-11T12:00:00Z",
      "freshnessStatus": "CURRENT",
      "confidence": "official_weekly"
    }
  ]
}
```

Finance semantics:

- `grossFreight` is customer or shipper freight revenue before costs.
- `passThroughAmount` is reimbursement or Vale-Pedagio cash flow that must not increase profit.
- `directTripCost` includes fuel, ARLA, non-reimbursed tolls, meals/lodging, and similar trip costs.
- `requiredReserves` includes maintenance, tires, taxes, insurance, replacement, emergency, and other configured reserve allocations.
- `safePersonalWithdrawal` is advisory and must be derived from deterministic calculation inputs.

## Expenses And Reserves

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/expenses` | Record direct or recurring expense. | Trip optional, category, amount, reimbursable flag, paid-by metadata. | `expense` resource. | `VALIDATION_ERROR`, `NOT_FOUND` |
| `GET` | `/api/expenses?from=&to=&category=` | List expenses for dashboard and finance inputs. | Date range, optional category. | `expenses[]`, totals. | `VALIDATION_ERROR` |
| `POST` | `/api/reserve-rules` | Create or update reserve allocation rule. | Bucket, percent/fixed/per-km rule, effective dates. | `reserveRule` resource. | `VALIDATION_ERROR`, `CONFLICT` |
| `GET` | `/api/reserve-wallets` | Return bucket balances, targets, and recent transactions. | None in ROU-217; `asOf` is reserved for later snapshots. | `reserveWallets[]`. | `UNAUTHENTICATED` |
| `POST` | `/api/reserve-allocations` | Request reserve allocation for freight payment or manual correction. | Payment/trip reference, amount, idempotency key. | Allocation request status. | `VALIDATION_ERROR`, `CONFLICT` |
| `GET` | `/api/financial-health-score` | Return current financial health summary. | None in ROU-217; `asOf` is reserved for later snapshots. | Score, status, components, trace ID. | `SNAPSHOT_PENDING` |

Example reserve allocation request:

```json
{
  "tripId": "trip_123",
  "freightPaymentId": "pay_123",
  "allocationSubjectId": "pay_123",
  "allocationRevision": 3,
  "grossAmount": "8000.00",
  "passThroughAmount": "600.00",
  "distanceKm": "1000.00",
  "currency": "BRL",
  "idempotencyKey": "reserve-trip_123-pay_123-v1",
  "reason": "FREIGHT_PAYMENT_RECEIVED",
  "requestedAt": "2026-05-11T12:00:10Z"
}
```

Example reserve allocation response:

```json
{
  "reserveAllocation": {
    "id": "reserve_alloc_123",
    "status": "ALLOCATED",
    "requestStatus": "ALLOCATED",
    "duplicate": false,
    "idempotencyKey": "reserve-trip_123-pay_123-v1",
    "grossAmount": "8000.00",
    "passThroughAmount": "600.00",
    "allocatableAmount": "7400.00",
    "requiredReserveAmount": "1110.00",
    "safePersonalWithdrawal": "1110.00",
    "currency": "BRL",
    "reason": "FREIGHT_PAYMENT_RECEIVED",
    "requestedAt": "2026-05-11T12:00:10Z",
    "bucketAllocations": {
      "MAINTENANCE": "592.00",
      "TIRES": "296.00",
      "TAXES_AND_DOCUMENTS": "222.00",
      "DRIVER_SALARY": "1110.00"
    },
    "transactions": [
      {
        "id": "reserve_txn_123",
        "bucket": "MAINTENANCE",
        "type": "CREDIT",
        "amount": "592.00",
        "balanceAfter": "1250.00",
        "currency": "BRL",
        "sourceType": "RESERVE_ALLOCATION",
        "sourceReference": "reserve-trip_123-pay_123-v1",
        "note": "Virtual reserve ledger movement. Not a real money transfer.",
        "createdAt": "2026-05-11T12:00:10Z"
      }
    ],
    "advisoryText": "Reserve allocations are virtual ledger movements, not real money transfers or legal/accounting advice."
  }
}
```

Example reserve wallet response:

```json
{
  "reserveWallets": [
    {
      "bucket": "MAINTENANCE",
      "currentBalance": "1250.00",
      "targetBalance": "5000.00",
      "currency": "BRL",
      "policy": "PERCENT_OF_AMOUNT",
      "lastAllocationAt": "2026-05-11T12:00:00Z",
      "transactions": [
        {
          "id": "reserve_txn_123",
          "bucket": "MAINTENANCE",
          "type": "CREDIT",
          "amount": "592.00",
          "balanceAfter": "1250.00",
          "currency": "BRL",
          "sourceType": "RESERVE_ALLOCATION",
          "sourceReference": "reserve-trip_123-pay_123-v1",
          "note": "Virtual reserve ledger movement. Not a real money transfer.",
          "createdAt": "2026-05-11T12:00:10Z"
        }
      ]
    }
  ]
}
```

Reserve semantics:

- Reserve allocations are virtual ledger movements unless a future payment integration explicitly implements real money movement.
- ROU-217 allocates synchronously in Core API as a temporary MVP persistence path. ROU-253 will wire the documented async flow where Core API publishes `reserve.allocation.requested`, Finance Worker owns the allocation calculation, and Core API persists worker allocation results idempotently.
- Manual corrections are auditable positive credit allocations in ROU-217. Debit or adjustment corrections require a later documented request shape before implementation.
- Repeated allocation requests with the same idempotency key must not double-credit buckets. Duplicate calls return `requestStatus: DUPLICATE_IGNORED` while the stored allocation remains `status: ALLOCATED`.
- Each account can have at most one active reserve rule per bucket; inactive rules are historical and do not participate in allocation.
- Reserve rule policy inputs are mutually exclusive: `PERCENT_OF_AMOUNT` accepts `rate`, `FIXED_AMOUNT` accepts `fixedAmount`, and `PER_KM` accepts `perKmAmount`.
- Active `PER_KM` reserve rules require `distanceKm` on allocation requests; missing distance must fail rather than silently allocate zero.
- ROU-217 stores `tripId`, `freightPaymentId`, and `allocationSubjectId` as references without foreign-key validation until ROU-215 introduces durable trip/profitability records.
- `allocationSubjectId` is required for every reserve allocation request and is the stable domain subject used for audit and future async event idempotency.
- Monetary request fields use decimal strings with at most 2 fraction digits; reserve rule `perKmAmount` uses at most 4 fraction digits and `rate` uses at most 6 fraction digits.
- `passThroughAmount` is excluded before bucket allocation and safe-withdrawal calculation.
- `safePersonalWithdrawal` is advisory and excludes required reserve buckets and pass-through cash flow.

## Fuel Prices And Fuel Estimates

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `GET` | `/api/fuel-prices/latest?fuel=DIESEL_S10&state=GO&city=GOIANIA` | Return latest normalized fuel price. | Fuel type, state, optional city. | Price and source metadata. | `NOT_FOUND`, `SOURCE_STALE` |
| `GET` | `/api/fuel-prices/history?fuel=DIESEL_S10&state=GO&city=GOIANIA` | Return historical price series. | Fuel type, state, optional city, date range. | `prices[]`. | `VALIDATION_ERROR` |
| `POST` | `/api/fuel-prices/driver-report` | Record driver-confirmed station price. | Fuel type, station metadata, price, receipt reference optional. | `driverFuelReport`. | `VALIDATION_ERROR` |
| `GET` | `/api/trucks/{truckId}/consumption-profile` | Return truck consumption assumptions. | Path `truckId`. | Loaded/empty km per liter, source window, confidence. | `NOT_FOUND` |
| `POST` | `/api/trips/{tripId}/fuel-estimate` | Estimate fuel budget for a trip. | Route km, truck consumption override optional, price override optional. | Fuel estimate with source metadata. | `VALIDATION_ERROR`, `NOT_FOUND` |

Example fuel estimate response:

```json
{
  "tripId": "trip_123",
  "routeKm": "850.00",
  "truckId": "truck_123",
  "loadStatus": "LOADED",
  "consumptionKmPerLiter": "2.35",
  "dieselPricePerLiter": "6.18",
  "estimatedLiters": "361.70",
  "estimatedFuelCost": "2235.31",
  "safetyMarginPercent": "10.00",
  "recommendedFuelBudget": "2458.84",
  "currency": "BRL",
  "priceSource": "ANP",
  "priceSourceType": "official_dataset",
  "priceConfidence": "official_weekly",
  "freshnessStatus": "CURRENT",
  "calculationTraceId": "calc_fuel_123"
}
```

Fuel semantics:

- Fuel is a direct trip cost, not a reserve percentage.
- Driver reports can override app estimates only when the calculation trace records the override.
- Stale or unknown ANP data must remain visible to the driver.

## Tolls And Vale-Pedagio

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/toll-estimates` | Estimate route toll costs. | Origin, destination, route optional, axle count, truck type. | Toll estimate and confidence. | `VALIDATION_ERROR`, `SOURCE_STALE` |
| `GET` | `/api/trips/{tripId}/tolls` | Return trip toll records. | Path `tripId`. | `tripTolls[]`, classification totals. | `NOT_FOUND` |
| `POST` | `/api/trips/{tripId}/tolls/manual-payment` | Record manual toll payment. | Plaza/name optional, amount, paid-by, reimbursement classification. | `tripToll`. | `VALIDATION_ERROR`, `NOT_FOUND` |
| `POST` | `/api/trips/{tripId}/vale-pedagio` | Record Vale-Pedagio proof and classification. | Provider/proof metadata, amount, received status. | `valePedagioRecord`. | `VALIDATION_ERROR`, `NOT_FOUND` |
| `GET` | `/api/toll-data/import-status` | Return toll dataset freshness and last import audit. | Optional source. | Import status. | `NOT_FOUND` |

Example toll estimate response:

```json
{
  "tripId": "trip_123",
  "route": {
    "origin": "Goiania, GO",
    "destination": "Sao Paulo, SP",
    "distanceKm": "920.00"
  },
  "vehicle": {
    "type": "truck",
    "axles": 6
  },
  "estimatedTolls": [
    {
      "name": "Praca de Pedagio Example",
      "highway": "BR-000",
      "state": "GO",
      "amount": "42.50",
      "currency": "BRL",
      "confidence": "matched_by_route_buffer",
      "classification": "PASS_THROUGH"
    }
  ],
  "totalEstimatedToll": "385.70",
  "currency": "BRL",
  "financeTreatment": "pass_through_or_reimbursement_not_profit",
  "freshnessStatus": "CURRENT"
}
```

Toll classification values:

- `PASS_THROUGH`
- `DRIVER_PAID_NON_REIMBURSED`
- `INCLUDED_IN_FREIGHT`
- `NO_TOLL`
- `UNKNOWN`

Toll semantics:

- Vale-Pedagio and toll reimbursement are pass-through money, not profit.
- `DRIVER_PAID_NON_REIMBURSED` tolls reduce profitability.
- `UNKNOWN` classification must keep the estimate conservative and visible.

## Compliance

Compliance APIs expose advisory metadata, reminders, and links to official channels. They must distinguish app-maintained metadata from official-source records.

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `GET` | `/api/compliance/profile` | Return compliance summary for the driver. | None. | RNTRC, CIOT, insurance, document, IPVA/licensing summary. | `UNAUTHENTICATED` |
| `PUT` | `/api/compliance/rntrc` | Store RNTRC metadata entered or confirmed by driver. | RNTRC number/category/status metadata. | `rntrcProfile`. | `VALIDATION_ERROR` |
| `POST` | `/api/compliance/rntrc/check-public-status` | Request safe public-status check when available. | RNTRC number/category, consent metadata if required. | Check status or unsupported response. | `SOURCE_UNAVAILABLE`, `NOT_IMPLEMENTED` |
| `POST` | `/api/compliance/insurance-policies` | Store insurance policy metadata. | Policy type, insurer, dates, annual premium, document reference optional. | `insurancePolicy`. | `VALIDATION_ERROR` |
| `GET` | `/api/compliance/insurance-policies` | List policy metadata. | Optional `active=true`. | `insurancePolicies[]`. | `UNAUTHENTICATED` |
| `GET` | `/api/compliance/score` | Return advisory compliance score. | Optional `asOf`. | Score, components, caveats. | `SNAPSHOT_PENDING` |
| `GET` | `/api/compliance/calendar` | Return compliance reminders. | Date range optional. | `calendarItems[]`. | `VALIDATION_ERROR` |
| `GET` | `/api/documents` | List stored document metadata. | Optional type/status filters. | `documents[]`. | `UNAUTHENTICATED` |
| `POST` | `/api/documents` | Create document metadata and upload handoff. | Type, owner reference, expiration, storage metadata. | Document metadata and upload instructions if applicable. | `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE` |

Example compliance profile response:

```json
{
  "driverId": "driver_123",
  "rntrc": {
    "numberMasked": "***1234",
    "category": "TAC",
    "status": "UNKNOWN",
    "source": "driver_entered",
    "lastCheckedAt": null,
    "officialActionUrl": "https://www.gov.br/antt",
    "advisoryText": "Confirm RNTRC status in official ANTT channels."
  },
  "ciot": {
    "required": "UNKNOWN",
    "latestTripStatus": "NOT_RECORDED",
    "advisoryText": "CIOT guidance is advisory until confirmed with official or professional sources."
  },
  "score": {
    "value": 72,
    "status": "ATTENTION",
    "components": [
      {
        "name": "insurance",
        "status": "CURRENT",
        "weight": "25.00"
      }
    ]
  }
}
```

Compliance semantics:

- `source=driver_entered` means Cofrete is storing app metadata, not asserting official truth.
- Public checks must not require or store gov.br credentials.
- Unsupported automation should return a clear advisory response instead of fake precision.

## Customers And Receivables

| Method | Path | Purpose | Request | Response | Error cases |
|---|---|---|---|---|---|
| `POST` | `/api/customers` | Create freight payer/customer profile. | Name, tax ID metadata optional, contact, payment terms. | `customer`. | `VALIDATION_ERROR`, `CONFLICT` |
| `GET` | `/api/customers/{customerId}/profitability` | Return customer-level profitability and payment behavior. | Path `customerId`, optional date range. | Margin, delay, toll/CIOT quality, caveats. | `NOT_FOUND`, `SNAPSHOT_PENDING` |
| `POST` | `/api/receivables` | Create receivable for freight payment. | Customer, trip, amount, due date, method, advance/balance type. | `receivable`. | `VALIDATION_ERROR`, `NOT_FOUND` |
| `GET` | `/api/receivables?status=overdue` | List receivables by status and date filters. | Status, date range optional. | `receivables[]`, totals. | `VALIDATION_ERROR` |
| `PUT` | `/api/receivables/{receivableId}/mark-paid` | Mark receivable paid. | Paid amount, paid date, method, note optional. | Updated `receivable`. | `VALIDATION_ERROR`, `NOT_FOUND`, `CONFLICT` |

Example receivable request:

```json
{
  "customerId": "cust_123",
  "tripId": "trip_123",
  "type": "BALANCE",
  "amount": "5000.00",
  "currency": "BRL",
  "dueDate": "2026-06-10",
  "paymentMethod": "PIX",
  "note": "Balance due after delivery."
}
```

Example customer profitability response:

```json
{
  "customerId": "cust_123",
  "periodStart": "2026-01-01",
  "periodEnd": "2026-05-31",
  "grossFreight": "32000.00",
  "expectedProfit": "5200.00",
  "averagePaymentDelayDays": "8.50",
  "lateReceivables": "2400.00",
  "currency": "BRL",
  "qualitySignals": {
    "ciotStatus": "MIXED",
    "valePedagioStatus": "MIXED",
    "loadingDelayStatus": "ATTENTION"
  },
  "advisoryText": "Customer profitability is based on Cofrete records and may not include all external obligations."
}
```

Receivable semantics:

- Receivables are tracking records, not bank integrations.
- Late payment affects cash-flow risk and customer profitability.
- Marking a receivable paid may trigger reserve allocation if the payment becomes allocatable.

## Validation And Error Conventions

All validation failures use HTTP `400` and this shape:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request contains invalid fields.",
  "details": [
    {
      "field": "grossFreight",
      "code": "MUST_BE_POSITIVE_DECIMAL",
      "message": "grossFreight must be greater than zero."
    }
  ],
  "correlationId": "corr_123"
}
```

Common error codes:

| HTTP | Error | Meaning |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Request shape, enum, date, decimal, or required-field problem. |
| `401` | `UNAUTHENTICATED` | Missing, expired, malformed, revoked, or invalid credentials. |
| `403` | `FORBIDDEN` | Valid credentials but missing role, account scope, company scope, or service permission. |
| `404` | `NOT_FOUND` | Resource does not exist or is not visible to the principal. |
| `409` | `CONFLICT` | Idempotency, duplicate, stale write, or invalid state transition conflict. |
| `415` | `UNSUPPORTED_MEDIA_TYPE` | Document upload metadata references unsupported content type. |
| `422` | `CALCULATION_UNAVAILABLE` | Valid request, but finance estimate cannot be produced with current inputs. |
| `422` | `SOURCE_STALE` | Source-backed result cannot be treated as current. |
| `422` | `SOURCE_UNAVAILABLE` | External source is unavailable or unsupported. |
| `425` | `SNAPSHOT_PENDING` | Async worker output has not arrived yet. |
| `429` | `RATE_LIMITED` | Caller exceeded rate limits. |
| `500` | `INTERNAL_ERROR` | Unexpected server failure. |

Validation rules:

- Unknown JSON fields should be rejected for write endpoints until compatibility policy is introduced.
- Decimal strings must use `.` as the decimal separator and must not include currency symbols.
- Enums are uppercase strings unless an external source requires another exact value.
- Date-only values use `YYYY-MM-DD`.
- Every response should include or propagate `correlationId` through logs and events.
- Write endpoints that can be retried by clients should accept an idempotency key in the request body or `Idempotency-Key` header when implementation reaches that endpoint.
