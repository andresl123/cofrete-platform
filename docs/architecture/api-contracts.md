# API Contracts

These contracts are implementation targets. They are versioned by documentation until OpenAPI generation is introduced.

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
