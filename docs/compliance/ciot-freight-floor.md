# CIOT And Minimum Freight Floor

Cofrete should track CIOT and freight-floor context as advisory pre-trip compliance support. It does not certify that a freight is legal or compliant.

## Pre-Trip Checklist

```text
CIOT required?
CIOT provided?
Freight above ANTT minimum estimate?
MDF-e/CIOT linked when applicable?
Payment method registered?
Vale-Pedagio confirmed?
Mandatory insurance proof confirmed?
```

## Risk Messages

| Missing or risky item | App warning |
|---|---|
| CIOT missing | "CIOT was not confirmed. Check the contract before starting the trip." |
| Freight below floor | "This freight may be below the ANTT minimum estimate." |
| Payment not registered | "Payment method was not confirmed. Cash-flow risk is high." |
| Missing Vale-Pedagio | "Vale-Pedagio was not confirmed. Tolls may reduce your cash flow." |

## Freight-Floor Checker

The MVP can start with a manual or placeholder comparison workflow while the official ANTT freight-floor table importer is designed.

Inputs:

| Input | Example |
|---|---|
| Origin and destination | Goiania to Santos |
| Distance | Loaded km plus empty return km |
| Vehicle | Number of axles |
| Cargo type | General, refrigerated, bulk, dangerous cargo |
| Operation type | Lotacao, fracionada, TAC-agregado |
| Offered freight | BRL amount |

Output:

```text
above_floor
below_floor
unknown
margin_amount
source
source_period
confidence
```

This is a high-risk compliance feature. Production automation needs source-backed tests, official-source review, and careful wording.
