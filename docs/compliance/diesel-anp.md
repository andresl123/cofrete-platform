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

| Confidence | Meaning |
|---|---|
| `driver_confirmed` | Driver's recent receipt or manual input |
| `fleet_negotiated` | Fleet/company contracted price for that user |
| `community_recent` | Recent nearby reports with enough samples |
| `official_weekly` | Latest ANP weekly municipality survey |
| `state_average` | Municipality unavailable, using ANP state average |
| `national_average` | Only Brazil average available |
| `unknown_or_stale` | No current trustworthy source |

## Truck Consumption Profiles

The fuel engine should learn from each truck instead of relying on one generic km/L value.

```text
truck_consumption_profile
truck_id
loaded_avg_km_l
empty_avg_km_l
last_30_days_avg_km_l
confidence
```

The trip calculator should use driver-entered values first, then truck history, then conservative defaults.

## Product Rules

- Mobile and web must not fetch ANP files directly.
- Fuel responses must include source, period, and freshness.
- Stale data should be visible in UI and calculations.
- Do not assume ANP provides a simple real-time diesel-price REST API.

Official ANP links are listed in `docs/compliance/official-source-register.md`.
