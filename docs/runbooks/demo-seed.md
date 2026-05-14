# Demo Seed Runbook

## Goal

Create one realistic synthetic demo driver, truck, freight, trip, reserve state, compliance profile, and dashboard state.

## Demo Data Requirements

- Autonomous Brazilian truck driver.
- One truck with synthetic plate/RENAVAM-style metadata.
- One freight/trip with gross freight, route, fuel assumptions, direct costs, tolls, and payment timing.
- ANP diesel fixture data.
- RNTRC and insurance metadata with advisory alerts.
- Reserve buckets and safe-withdrawal result.
- Receivable status.

## Synthetic Seed

The current MVP demo seed lives at:

```text
scripts/demo/cofrete-mvp-demo-seed.json
```

It contains one synthetic TAC driver in GO, one synthetic six-axle truck, one GO to SP freight, deterministic trip-cost inputs, reserve policy, RNTRC/insurance metadata, ANP fixture expectations, and dashboard expectations.

Seed/reset commands:

```sh
scripts/demo/seed-demo-data.sh
scripts/demo/reset-demo-data.sh
```

Validation command:

```sh
scripts/smoke/local-demo-e2e.sh
```

The fixture asserts exact expected values:

- gross freight: `8000.00 BRL`
- pass-through amount: `600.00 BRL`
- direct trip cost: `3000.00 BRL`
- required reserves: `1776.00 BRL`
- expected profit and safe personal withdrawal: `1974.00 BRL`

## Rules

- Demo data must be synthetic.
- Do not include real CPF, CNPJ, plate, RNTRC, policy, or customer data.
- Demo scripts should be repeatable and resettable.
