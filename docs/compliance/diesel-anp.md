# Diesel And ANP Data

Fuel prices are a major cost input. Cofrete should import ANP diesel datasets server-side and expose normalized data through Core API.

## Source Priority

1. Driver's own latest receipt/manual input.
2. Fleet or company negotiated fuel price.
3. Community-reported price near route, when supported.
4. ANP latest municipality average.
5. ANP latest state average.
6. Brazil national average fallback.

## Formula

```text
fuel_cost = route_km / km_per_liter * diesel_price
```

## Confidence Levels

- `driver_receipt`: user-provided recent receipt.
- `fleet_price`: negotiated price from fleet/company.
- `official_weekly_city`: ANP city-level weekly data.
- `official_weekly_state`: ANP state-level weekly data.
- `official_weekly_national`: ANP national fallback.
- `unknown_or_stale`: no current trustworthy source.

## Product Rules

- Mobile and web must not fetch ANP files directly.
- Fuel responses must include source, period, and freshness.
- Stale data should be visible in UI and calculations.

Official ANP links are listed in `docs/compliance/official-source-register.md`.
