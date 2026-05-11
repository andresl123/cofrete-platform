# Brazil Trucker Finance Blueprint

Cofrete is a Brazil-localized financial health assistant for truck drivers, especially autonomous drivers, MEI Caminhoneiro drivers, and small fleet owners.

The core product promise is simple: do not show only gross freight. Show how much money is safe after direct trip costs and future obligations.

Example result:

```text
Gross freight: R$ 8,000
Real trip cost: R$ 4,900
Required reserves: R$ 1,600
Safe personal withdrawal: R$ 1,100
Financial health: Good
```

## Reserve Buckets

Every freight or payment should be classified into explicit buckets:

| Bucket | Purpose |
|---|---|
| Fuel / ARLA / toll cash flow | Immediate trip expenses |
| Maintenance reserve | Oil, filters, brakes, suspension, corrective repairs |
| Tire reserve | Tire replacement, recap, alignment, balancing |
| Insurance reserve | Truck insurance and transport liability policies |
| Taxes and documents | MEI DAS, IR support, IPVA, licensing, accountant, certificates |
| Truck replacement fund | Future down payment or truck purchase |
| Emergency fund | Breakdowns, medical days off, late payments |
| Driver salary | Personal/family money that is safe to use |
| Profit | Business growth, debt reduction, investment |

## Brazil-Specific Modules

- MEI Caminhoneiro limit monitoring.
- RNTRC profile storage and official-channel guidance.
- CIOT and pre-trip freight regularity checklist.
- Toll and Vale-Pedagio classification.
- ANP diesel price importer and confidence levels.
- Insurance and document expiration alerts.
- Receivables tracking for late customer payments.

## Non-Goals

- Cofrete does not file taxes.
- Cofrete does not update RNTRC or gov.br records.
- Cofrete does not issue insurance policies.
- Cofrete does not guarantee freight legality or regulatory compliance.

The app is advisory product software. Users must confirm production decisions with official government channels and qualified legal, tax, accounting, or insurance professionals.

## Source Notes

This repository doc summarizes `/home/andre/Downloads/brazil_trucker_finance_app_blueprint_validated.md` and links official sources in `docs/compliance/official-source-register.md`.
