# Runtime Smoke Tests

Runtime smoke tests prove that the most important Cofrete flows work after service scaffolds exist. They should be small, repeatable, and safe to run against local or staging environments.

## Implemented Scripts

```text
scripts/smoke/local-demo-e2e.sh
scripts/smoke/validate_mvp_demo_flow.py
```

`scripts/smoke/local-demo-e2e.sh` runs the synthetic fixture smoke validator and the narrow service/mobile test set that proves the current MVP path. It skips `web-app` tests until `web-app/package.json` exists.

## Local Demo E2E

Command:

```sh
scripts/demo/seed-demo-data.sh
scripts/smoke/local-demo-e2e.sh
scripts/demo/reset-demo-data.sh
```

Coverage:

- synthetic demo driver and truck fixture exists.
- freight/trip cost input has exact expected deterministic finance output.
- toll reimbursement and Vale-Pedagio are proved as pass-through money and do not increase profit.
- ANP synthetic diesel fixture returns the latest expected GOIANIA DIESEL_S10 price for the demo period.
- RNTRC metadata starts as advisory `UNKNOWN` and links back to official ANTT consultation.
- insurance metadata produces an expiration-alert scenario.
- mobile dashboard test renders financial health score, reserve, receivable, compliance, and pass-through caveats.

Safe cleanup:

- `scripts/demo/seed-demo-data.sh` copies only `scripts/demo/cofrete-mvp-demo-seed.json` to `.tmp/`.
- `scripts/demo/reset-demo-data.sh` removes that `.tmp/` copy.
- no script writes production data, external credentials, or persistent service state.

## Planned Split Scripts

The local demo smoke currently exercises these checks through one wrapper. Split them into dedicated scripts only when the required service runtime and safe cleanup path exist:

- `finance-calculation-smoke.sh`
- `data-importer-smoke.sh`
- `compliance-center-smoke.sh`

## Finance Calculation Smoke

Expected coverage:

- known freight input.
- known expenses.
- known reserve rules.
- exact expected result.
- toll reimbursement does not increase profit.

## Data Importer Smoke

Expected coverage:

- sample ANP file can be parsed.
- sample toll-plaza file can be parsed when available.
- import audit is created.
- internal API returns imported data.

## Compliance Center Smoke

Expected coverage:

- RNTRC field can be saved.
- insurance policy can be saved.
- expiration alert is generated.
- official gov.br/ANTT guidance text is present.

## Rule

Do not add a runtime smoke script without documenting its required services, data setup, validation command, and safe cleanup path.
