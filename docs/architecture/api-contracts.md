# API Contracts

These contracts are implementation targets. They are versioned by documentation until OpenAPI generation is introduced.

## Profile

```http
POST /api/drivers
GET /api/drivers/me
PATCH /api/drivers/me
POST /api/trucks
GET /api/trucks
PATCH /api/trucks/{truckId}
```

## Freight And Trip

```http
POST /api/freights
POST /api/trips
POST /api/trips/{tripId}/profitability-estimate
```

Profitability estimate responses must include gross freight, direct trip cost, required reserves, safe personal withdrawal, financial health status, currency, and calculation trace ID.

## Reserves

```http
GET /api/reserve-buckets
POST /api/reserve-buckets
GET /api/reserve-transactions
POST /api/reserve-transactions
GET /api/safe-withdrawal/latest
```

## Fuel Prices

```http
GET /api/fuel-prices/latest
POST /api/fuel-prices/driver-report
POST /api/trips/{tripId}/fuel-estimate
```

Fuel responses must include source, source type, period, confidence, and freshness metadata.

## Tolls And Vale-Pedagio

```http
POST /api/routes/toll-estimate
GET /api/trips/{tripId}/tolls
POST /api/trips/{tripId}/tolls
PATCH /api/trips/{tripId}/tolls/{tollId}
```

Toll records must classify pass-through, driver-paid, included-in-freight, no-toll, and unknown states.

## Compliance

```http
GET /api/compliance/summary
PATCH /api/compliance/rntrc
POST /api/compliance/insurance
PATCH /api/compliance/insurance/{policyId}
```

Compliance responses must use advisory wording and official-source links.

## Customers And Receivables

```http
GET /api/customers
POST /api/customers
GET /api/receivables
POST /api/receivables
PATCH /api/receivables/{receivableId}
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
