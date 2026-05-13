# API Contracts

These contracts are implementation targets. They are versioned by documentation until OpenAPI generation is introduced.

## Auth And Sessions

Auth endpoints are owned by Core API. Cofrete app login is separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, or other official systems.

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET /api/auth/me
```

All non-auth product APIs require an authenticated Cofrete principal unless an implementation issue explicitly documents a public endpoint.

### Principal Model

Authenticated principals use this shape:

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

### `POST /api/auth/register`

Creates a Cofrete application identity only. Driver profile, truck, tax, and compliance data remain owned by their profile APIs.

Example request:

```json
{
  "email": "driver@example.com",
  "password": "example-password",
  "displayName": "Example Driver",
  "clientType": "MOBILE"
}
```

Example response:

```json
{
  "principal": {
    "id": "user_123",
    "type": "DRIVER",
    "accountId": "acct_123",
    "companyId": null,
    "roles": ["DRIVER"]
  }
}
```

Rules:

- MVP self-registration creates a `DRIVER` principal by default.
- `COMPANY_ADMIN` assignment must remain disabled until a future company-mode issue implements that workflow.
- `SUPPORT` and `PLATFORM_ADMIN` assignment must be administrative and auditable, not public self-registration.
- If Cofrete stores passwords directly, store only Argon2id or bcrypt password hashes.

### `POST /api/auth/login`

Authenticates Cofrete application credentials and creates a refresh session.

Example request:

```json
{
  "email": "driver@example.com",
  "password": "example-password",
  "clientType": "MOBILE"
}
```

Example response:

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

Rules:

- Mobile clients should receive refresh material only in a form suitable for platform secure storage.
- Web admin should prefer secure, HTTP-only, SameSite refresh cookies when same-site deployment allows it.
- Login failure responses should not reveal whether the email exists.

### `POST /api/auth/refresh`

Rotates or validates the active refresh session and returns a new access token.

Example request for bearer-token refresh clients:

```json
{
  "refreshToken": "refresh_token"
}
```

Cookie-based web clients may send the refresh session as a secure HTTP-only cookie instead of a JSON `refreshToken`.

Example response:

```json
{
  "accessToken": "jwt_or_opaque_access_token",
  "tokenType": "Bearer",
  "expiresInSeconds": 900,
  "refreshToken": "rotated_refresh_token_returned_only_when_client_type_allows_it",
  "principal": {
    "id": "user_123",
    "type": "DRIVER",
    "accountId": "acct_123",
    "companyId": null,
    "roles": ["DRIVER"]
  }
}
```

### `POST /api/auth/logout`

Revokes the active refresh session. Access tokens remain short-lived and should naturally expire.

Example request:

```json
{
  "refreshToken": "refresh_token_when_not_cookie_based"
}
```

Example response:

```json
{
  "revoked": true
}
```

### `GET /api/auth/me`

Returns the authenticated Cofrete principal and linked application profile references.

Example response:

```json
{
  "principal": {
    "id": "user_123",
    "type": "DRIVER",
    "accountId": "acct_123",
    "companyId": null,
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
- Workers authenticate as internal services with least privilege, not as users.

Auth error examples:

Missing, expired, malformed, revoked, or invalid credentials should return HTTP `401` with `UNAUTHENTICATED`:

```json
{
  "error": "UNAUTHENTICATED",
  "message": "Authentication is required.",
  "details": [],
  "correlationId": "corr_123"
}
```

Valid credentials without the required role, account scope, company scope, or service permission should return HTTP `403` with `FORBIDDEN`:

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
