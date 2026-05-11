# Brazil Trucker Finance Blueprint

Cofrete is a Brazil-localized financial operating system for truck drivers, especially autonomous drivers, MEI Caminhoneiro drivers, TAC operators, and small fleet owners.

The core product promise is simple: do not show only gross freight. Show how much money is safe after direct trip costs and future obligations.

Example result:

```text
Gross freight: R$ 8,000
Real trip cost: R$ 4,900
Required reserves: R$ 1,600
Safe personal withdrawal: R$ 1,100
Financial health: Good
```

## Target Operating Profiles

The first product path serves an autonomous driver with one truck. The MVP should support MEI Caminhoneiro first and Pessoa Fisica autonoma soon after.

| Profile | Product implication |
|---|---|
| MEI Caminhoneiro | Track DAS, yearly limit, DASN-SIMEI reminders, and simple reserve rules |
| Pessoa Fisica autonoma | Track gross receipts, planning-only taxable portion, and IR/Carne-Leao reminders |
| TAC | Track RNTRC, CIOT, freight floor context, insurance proof, and Vale-Pedagio status |
| ME / Simples Nacional | Support accountant-driven tax settings and invoice workflow later |
| Small fleet owner | Add vehicle-level profitability and fleet reserve planning after the single-driver MVP |

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

Fuel is not a reserve percentage. Fuel must be calculated per trip from route distance, truck consumption, and diesel price.

## Brazil-Specific Modules

- MEI Caminhoneiro limit monitoring.
- RNTRC profile storage and official-channel guidance.
- CIOT and pre-trip freight regularity checklist.
- ANTT minimum freight floor comparison, starting with a manual or placeholder workflow.
- Toll and Vale-Pedagio classification.
- ANP diesel price importer and confidence levels.
- Loading/unloading waiting-time tracking.
- IPVA/licensing reminders using state-specific rules.
- Pessoa Fisica autonoma tax-planning support.
- Insurance and document expiration alerts.
- Receivables tracking for late customer payments.

## Finance Engine Rules

Safe personal money:

```text
Gross freight
- pass-through items
- direct trip costs
- taxes
- required reserves
- debt/financing
```

Trip profitability:

```text
freight
- diesel
- ARLA
- non-reimbursed tolls
- meals/lodging
- maintenance reserve
- tire reserve
- tax reserve
- insurance reserve
- financing allocation
- replacement reserve
```

Default reserves are temporary until real driver/truck history exists. Starter rules may use percentages for maintenance, tires, insurance, IPVA/licensing, taxes, replacement, and emergency reserve. After 60 to 90 days, the app should prefer real cost-per-km, annual premiums, state/year rules, and explicit truck replacement goals.

Vale-Pedagio and toll reimbursement are tracked separately from profit and safe withdrawal.

## Trip Decision Output

The new freight calculator should explain:

- revenue per loaded km and total km;
- fuel cost and price confidence;
- maintenance, tire, insurance, tax, and replacement reserves;
- toll/Vale-Pedagio treatment;
- CIOT, insurance, and freight-floor risks;
- cash-flow risk from advances, balance, and late payment terms;
- recommendation: accept, renegotiate, or reject.

## Product Modules

The blueprint is organized around these product modules:

| Module | Purpose |
|---|---|
| Onboarding | Driver, truck, tax profile, state, RNTRC, insurance, financing, and income target |
| Freight calculator | Profitability, safe withdrawal, cash-flow risk, and compliance risk before accepting a load |
| Reserve wallet | Virtual envelopes for truck costs, documents, replacement, emergency, and driver salary |
| Receivables | Advance, balance, Pix, boleto/transfer, Vale-Pedagio, late payments, and reimbursement tracking |
| Maintenance intelligence | Cost per km, preventive/corrective history, tire lifecycle, downtime, and next service |
| Customer profitability | Payment delay, loading delay, margin, CIOT/Vale-Pedagio quality, and dispute history |
| Compliance calendar | MEI DAS, DASN-SIMEI, IPVA, CRLV/licensing, insurance, RNTRC, CNH, and service reminders |

## Suggested Screens

The mobile product should prioritize:

- home dashboard with gross revenue, real net profit, safe withdrawal, reserves, obligations, and scores;
- new freight calculator;
- reserve wallet;
- truck profile and maintenance history;
- customer profitability;
- compliance center with RNTRC/gov.br guidance, insurance, IPVA/licensing, CNH, and official action links.

## Non-Goals

- Cofrete does not file taxes.
- Cofrete does not update RNTRC or gov.br records.
- Cofrete does not issue insurance policies.
- Cofrete does not guarantee freight legality or regulatory compliance.

The app is advisory product software. Users must confirm production decisions with official government channels and qualified legal, tax, accounting, or insurance professionals.

## Source Notes

This repository doc summarizes `/home/andre/Downloads/brazil_trucker_finance_app_blueprint_validated.md` and links official sources in `docs/compliance/official-source-register.md`.
