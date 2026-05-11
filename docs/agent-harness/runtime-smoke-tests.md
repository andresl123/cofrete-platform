# Runtime Smoke Tests

Runtime smoke tests prove that the most important Cofrete flows work after service scaffolds exist. They should be small, repeatable, and safe to run against local or staging environments.

## Planned Scripts

```text
scripts/smoke/local-demo-e2e.sh
scripts/smoke/finance-calculation-smoke.sh
scripts/smoke/data-importer-smoke.sh
scripts/smoke/compliance-center-smoke.sh
```

## Local Demo E2E

Expected coverage:

- local infrastructure starts.
- demo driver can be created.
- demo truck can be created.
- freight can be entered.
- trip profitability can be calculated.
- reserve allocation is produced.
- dashboard returns a financial health result.

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
