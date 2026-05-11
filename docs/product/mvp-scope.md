# MVP Scope

## Target User

The MVP serves an autonomous Brazilian truck driver or owner-operator with one truck who needs to decide whether a freight is worth accepting and how much money can be safely withdrawn after business obligations.

MEI Caminhoneiro is the first tax profile. Pessoa Fisica autonoma support should follow soon after because many autonomous drivers do not operate as MEI.

## In Scope

- Driver and truck profile setup.
- Tax profile, MEI Caminhoneiro metadata, and configurable year-based tax rules.
- Freight and trip entry.
- Fuel, toll, direct cost, reserve, profit, and safe-withdrawal calculation.
- Reserve wallet with bucket balances and transaction history.
- ANP diesel-price fixture import and latest-price API.
- Toll and Vale-Pedagio classification.
- Manual/preliminary ANTT minimum freight floor comparison.
- CIOT and pre-trip regularity checklist.
- RNTRC, insurance, IPVA/licensing, CNH, and document metadata tracking.
- Loading/unloading waiting-time metadata and advisory alerts.
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
- Automated ANTT freight-floor table ingestion until source reliability and validation are designed.
- Direct Pix/Open Finance integration.
- AI receipt OCR.

## Success Criteria

- A driver can enter one freight/trip and see deterministic profitability, reserves, and safe withdrawal.
- Tolls and Vale-Pedagio never inflate profit.
- Fuel price source and confidence are visible.
- Compliance screens use advisory wording and link to official sources.
- CIOT, freight-floor, insurance, and waiting-time risks can be represented without official automation.
- Demo data can exercise the whole MVP without external credentials.
