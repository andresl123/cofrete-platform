# API Contracts

These contracts are implementation targets. They are versioned by documentation until OpenAPI generation is introduced.

## Auth And Sessions

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET /api/auth/me
```

Auth endpoints are owned by Core API. Cofrete app login is separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, or other official systems.

All non-auth product APIs require an authenticated Cofrete principal unless an implementation issue explicitly documents a public endpoint.

Example login request:

```json
{
  "email": "driver@example.com",
  "password": "example-password"
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
    "roles": ["DRIVER"]
  }
}
```

Example refresh request:

```json
{
  "refreshToken": "refresh_token"
}
```

Example `/api/auth/me` response:

```json
{
  "principal": {
    "id": "user_123",
    "type": "DRIVER",
    "accountId": "acct_123",
    "roles": ["DRIVER"]
  },
  "driverProfileId": "driver_123",
  "companyId": null
}
```

Auth rules:

- Access tokens must be short-lived.
- Refresh tokens or refresh sessions must be revocable.
- Logout must revoke the active refresh session.
- Tokens must carry only authorization metadata, not sensitive driver, vehicle, document, or financial data.
- Mobile clients should store secrets only in platform secure storage.
- Web admin should prefer secure, HTTP-only, SameSite cookies when deployment shape allows it.
- Support and platform-admin access must be auditable.
- Service-to-service credentials are separate from user login sessions.

Auth error examples:

```json
{
  "error": "UNAUTHENTICATED",
  "message": "Authentication is required.",
  "details": [],
  "correlationId": "corr_123"
}
```

```json
{
  "error": "FORBIDDEN",
  "message": "You do not have access to this resource.",
  "details": [],
  "correlationId": "corr_124"
}
```

## Profile And Tax

```http
POST /api/drivers
GET /api/drivers/me
PATCH /api/drivers/me
POST /api/trucks
GET /api/trucks
GET /api/trucks/{truckId}
PUT /api/trucks/{truckId}
POST /api/tax-profile
GET /api/tax-profile
```

## Freight And Trip

```http
POST /api/trips
GET /api/trips/{tripId}
POST /api/trips/{tripId}/profitability-estimate
GET /api/trips/{tripId}/profitability-snapshot
POST /api/trips/{tripId}/acceptance-decision
```

Profitability estimate responses must include gross freight, direct trip cost, required reserves, safe personal withdrawal, financial health status, currency, and calculation trace ID.

Freight may become a separate aggregate later, but the MVP API starts from trip entry because the driver-facing workflow is "new freight/trip decision."

## Expenses And Reserves

```http
POST /api/expenses
GET /api/expenses?from=&to=&category=
POST /api/reserve-rules
GET /api/reserve-wallets
POST /api/reserve-allocations
GET /api/financial-health-score
```

## Fuel Prices

```http
GET /api/fuel-prices/latest?fuel=DIESEL_S10&state=GO&city=GOIANIA
GET /api/fuel-prices/history?fuel=DIESEL_S10&state=GO&city=GOIANIA
POST /api/fuel-prices/driver-report
GET /api/trucks/{truckId}/consumption-profile
POST /api/trips/{tripId}/fuel-estimate
```

Fuel responses must include source, source type, period, confidence, and freshness metadata.

Example fuel estimate response:

```json
{
  "routeKm": 850,
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
  "priceConfidence": "official_weekly"
}
```

## Tolls And Vale-Pedagio

```http
POST /api/toll-estimates
GET /api/trips/{tripId}/tolls
POST /api/trips/{tripId}/tolls/manual-payment
POST /api/trips/{tripId}/vale-pedagio
GET /api/toll-data/import-status
```

Toll records must classify pass-through, driver-paid, included-in-freight, no-toll, and unknown states.

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
      "confidence": "matched_by_route_buffer"
    }
  ],
  "totalEstimatedToll": "385.70",
  "currency": "BRL",
  "financeTreatment": "pass_through_or_reimbursement_not_profit"
}
```

## Compliance

```http
GET /api/compliance/profile
PUT /api/compliance/rntrc
POST /api/compliance/rntrc/check-public-status
POST /api/compliance/insurance-policies
GET /api/compliance/insurance-policies
GET /api/compliance/score
GET /api/compliance/calendar
GET /api/documents
POST /api/documents
```

Compliance responses must use advisory wording and official-source links.

## Customers And Receivables

```http
POST /api/customers
GET /api/customers/{customerId}/profitability
POST /api/receivables
GET /api/receivables?status=overdue
PUT /api/receivables/{receivableId}/mark-paid
```

## Error Shape

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Human-readable summary",
  "details": [],
  "correlationId": "corr_123"
}
```
