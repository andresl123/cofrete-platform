# MVP Scope

## Target User

The MVP serves an autonomous Brazilian truck driver or owner-operator who needs to decide whether a freight is worth accepting and how much money can be safely withdrawn after business obligations.

## In Scope

- Driver and truck profile setup.
- Tax profile and MEI Caminhoneiro metadata.
- Freight and trip entry.
- Fuel, toll, direct cost, reserve, profit, and safe-withdrawal calculation.
- Reserve wallet with bucket balances and transaction history.
- ANP diesel-price fixture import and latest-price API.
- Toll and Vale-Pedagio classification.
- RNTRC, insurance, and document metadata tracking.
- Receivables and customer payment tracking.
- Mobile-first dashboard with financial health status.
- Demo seed and smoke validation flow.

## Out Of Scope For MVP

- Real payment movement or bank integration.
- Automated government account login or gov.br integration.
- Official RNTRC updates from inside Cofrete.
- Tax filing, insurance purchasing, or legal compliance certification.
- Real-time route optimization.
- Production-grade paid toll API integration.

## Success Criteria

- A driver can enter one freight/trip and see deterministic profitability, reserves, and safe withdrawal.
- Tolls and Vale-Pedagio never inflate profit.
- Fuel price source and confidence are visible.
- Compliance screens use advisory wording and link to official sources.
- Demo data can exercise the whole MVP without external credentials.
