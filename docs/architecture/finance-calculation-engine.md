# Finance Calculation Engine

The Finance Worker deterministic calculation engine turns an explicit trip input snapshot into an auditable trip finance output. It is advisory product software, not tax, legal, accounting, insurance, ANTT, ANP, Receita Federal, DETRAN, SEFAZ, or insurer guidance.

## Determinism Boundary

The pure calculation module accepts a complete `TripFinanceInputSnapshot` and returns a `TripFinanceCalculationResult`.

The same input snapshot must produce the same:

- gross freight;
- pass-through amount;
- direct trip cost;
- required reserves;
- safe personal withdrawal;
- expected profit;
- financial health impact;
- calculation trace ID;
- trace entries.

The calculator does not read clocks, databases, environment variables, network services, or random values. Event handling may attach the original request timestamp to the emitted event, but the money calculation itself depends only on the input snapshot.

## Input Categories

The MVP calculation snapshot uses decimal strings for all money-like and ratio values.

Revenue:

- `grossFreight`: contracted freight revenue.

Route and fuel:

- `totalDistanceKm`: total trip distance used for fuel planning.
- `dieselConsumptionKmPerLiter`: truck consumption assumption.
- `dieselPricePerLiter`: diesel price assumption.

Pass-through money:

- `tollReimbursement`;
- `valePedagio`;
- `otherPassThrough`.

Pass-through money is tracked for cash-flow visibility but excluded from profit and safe-withdrawal calculations.

Direct trip cost:

- calculated fuel cost;
- `arlaCost`;
- `nonReimbursedToll`;
- `mealsAndLodgingCost`;
- `otherDirectCost`.

Required reserve policy:

- maintenance;
- tires;
- taxes and documents;
- insurance;
- truck replacement;
- emergency.

Other obligations:

- `financingAllocation`.

Source freshness:

- `sourceFreshness` carries labels such as `CURRENT`, `STALE`, `FAILED`, or `UNKNOWN` for source-backed assumptions.

## Formulas

Fuel:

```text
dieselLiters = totalDistanceKm / dieselConsumptionKmPerLiter
fuelCost = dieselLiters * dieselPricePerLiter
```

Pass-through:

```text
passThroughAmount = tollReimbursement + valePedagio + otherPassThrough
```

Reserve base:

```text
reserveBase = grossFreight - passThroughAmount
```

Direct trip cost:

```text
directTripCost = fuelCost + arlaCost + nonReimbursedToll + mealsAndLodgingCost + otherDirectCost
```

Required reserves:

```text
maintenanceReserve = reserveBase * maintenanceRate
tireReserve = reserveBase * tireRate
taxReserve = reserveBase * taxRate
insuranceReserve = reserveBase * insuranceRate
replacementReserve = reserveBase * replacementRate
emergencyReserve = reserveBase * emergencyRate

requiredReserves =
  maintenanceReserve
  + tireReserve
  + taxReserve
  + insuranceReserve
  + replacementReserve
  + emergencyReserve
```

Profit and safe withdrawal:

```text
expectedProfit =
  grossFreight
  - passThroughAmount
  - directTripCost
  - requiredReserves
  - financingAllocation

safePersonalWithdrawal = max(expectedProfit, 0.00)
```

## Rounding

- Output monetary values are decimal strings with two fractional digits.
- Monetary inputs must be non-negative and cent-scale where they represent money.
- Fuel liters use internal 8-decimal calculation scale.
- Fuel cost is rounded to cents with `HALF_UP`.
- Each reserve bucket is rounded to cents with `HALF_UP` before reserve buckets are summed.
- Rates are non-negative decimals from `0` to `1`.
- MVP currency is `BRL`.

## Financial Health Impact

The MVP worker classifies the result from `expectedProfit` and `reserveBase`:

| Impact | Rule |
|---|---|
| `GOOD` | `expectedProfit / reserveBase >= 0.10` |
| `ATTENTION` | `expectedProfit >= 0.00` and margin is below `0.10` |
| `RISK` | `expectedProfit < 0.00` |
| `UNKNOWN` | `reserveBase == 0.00` |

## Trace Output

The calculation result includes:

- `calculationTraceId`: deterministic SHA-256-derived ID over canonical input and output assumptions.
- `trace`: ordered entries for fuel, pass-through, reserve base, direct trip cost, required reserves, expected profit, and safe personal withdrawal.

Trace output is intended for support and test fixtures. It must not include secrets or unnecessary CPF/CNPJ, RENAVAM, plate, RNTRC, bank, or payment identifiers.

## Event Handling

`trip.recalculation.requested` carries IDs and `inputRevision`, not the full money snapshot. Finance Worker therefore separates:

- event validation and idempotency handling;
- loading an explicit trip finance input snapshot;
- pure deterministic calculation;
- publication of `trip.finance.recalculated`.

The current worker provides a `TripFinanceInputSnapshotProvider` extension point. Until Core API or a durable read model supplies authoritative snapshots, the default provider fails explicitly instead of fabricating finance results.

`trip.finance.recalculated` uses:

- routing key `trip.finance.recalculated`;
- producer `finance-worker`;
- correlation ID copied from the request;
- idempotency key `trip:{tripId}:finance-result:{inputRevision}:{calculationTraceId}`;
- decimal-string monetary values;
- source freshness copied from the input snapshot.
