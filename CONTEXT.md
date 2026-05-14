# Cofrete Platform Context

Cofrete is a financial health and compliance assistant for Brazilian truck drivers and owner-operators. This context records the canonical language used across product, architecture, compliance, and agent documentation.

## Language

**Canonical Markdown Doc**:
A markdown document that owns the durable source-of-truth wording for product scope, architecture, compliance, contracts, operations, or agent workflow.
_Avoid_: generated page, visual explainer

**Explainer Page**:
A manually maintained, derived HTML summary of canonical markdown docs.
_Avoid_: source of truth, canonical doc

**Derived Entry Point**:
A reader-friendly orientation page that helps people choose which canonical docs to read next.
_Avoid_: canonical reference

**Agent Harness Check**:
A lightweight repository validation script that confirms required foundation files and documentation guardrails are present.
_Avoid_: full test suite, content drift detector

**Reserve Wallet**:
A virtual set of business envelopes that tracks balances by reserve purpose for a driver account.
_Avoid_: bank account, payment wallet, cash account

**Reserve Bucket**:
A named reserve purpose such as maintenance, tires, taxes/documents, truck replacement, emergency, driver salary, or profit.
_Avoid_: category, account, fund

**Reserve Allocation**:
A deterministic virtual ledger split of allocatable freight money into reserve buckets.
_Avoid_: payment transfer, payout, settlement

**Pass-Through Cash Flow**:
Money such as toll reimbursement or Vale-Pedagio that moves through the driver but must not increase profit or safe withdrawal.
_Avoid_: revenue, profit, income

**Safe Personal Withdrawal**:
The advisory amount a driver may plan to use personally after pass-through cash flow and required reserves are excluded.
_Avoid_: guaranteed income, disposable income, legal availability

**Freight Customer**:
A payer or contracting party the driver tracks for receivable and profitability planning.
_Avoid_: bank customer, Cofrete customer account

**Receivable**:
An app-maintained record of freight money expected from a freight customer, such as an advance, balance, or reimbursement.
_Avoid_: bank payment, guaranteed collection, settlement

**Customer Payment Tracking**:
Advisory cash-flow tracking for expected, late, partial, paid, or canceled receivables.
_Avoid_: payment processing, bank integration, legal collection

**Compliance Profile**:
An advisory Cofrete view of driver-entered and Cofrete-generated compliance metadata, reminders, and official-channel links.
_Avoid_: official ANTT record, legal status certificate, government profile

**Compliance Alert**:
An advisory warning that a compliance-related record may need driver review, renewal, or official-channel confirmation.
_Avoid_: official violation, enforcement notice, certified legal finding

**Compliance Calendar**:
A planning calendar of document, insurance, RNTRC, tax, and vehicle-obligation reminders.
_Avoid_: legal deadline authority, official calendar, guaranteed obligation list

**Freight-Floor Source Period**:
An app record for one effective official ANTT freight-floor publication and its parsed or manually approved coefficient set.
_Avoid_: legal certification, permanent table

**Freight-Floor Check**:
An advisory comparison of an offered freight against a source-backed or manually approved ANTT freight-floor period.
_Avoid_: official ANTT validation, enforcement decision, guaranteed legal compliance

## Relationships

- An **Explainer Page** summarizes one or more **Canonical Markdown Docs**.
- An **Explainer Page** can be used as a **Derived Entry Point** for faster orientation.
- A **Canonical Markdown Doc** should be updated before its manually maintained **Explainer Page** is realigned.
- The **Agent Harness Check** verifies that required **Explainer Pages** exist and identify themselves as manually maintained derived summaries.
- A **Reserve Wallet** contains one or more **Reserve Buckets**.
- A **Reserve Allocation** creates virtual ledger movement into one or more **Reserve Buckets**.
- **Pass-Through Cash Flow** is excluded before calculating **Safe Personal Withdrawal**.
- A **Freight Customer** can have one or more **Receivables**.
- **Customer Payment Tracking** records receivable state for planning and does not move money.
- A **Compliance Profile** can surface one or more **Compliance Alerts** and **Compliance Calendar** reminders.
- A **Freight-Floor Check** uses one **Freight-Floor Source Period** when source confidence is sufficient.
- A **Freight-Floor Check** returns `unknown` when source, cargo, operation, axle, or effective-period semantics cannot be proven.

## Example dialogue

> **Dev:** "Should I edit the architecture explainer to change service ownership?"
> **Domain expert:** "No. Update the **Canonical Markdown Doc** first, then manually realign the derived **Explainer Page** so it stays accurate."

> **Dev:** "Will the harness prove the explainer content is current?"
> **Domain expert:** "No. The **Agent Harness Check** only verifies the **Explainer Page** guardrail text exists; humans still realign the content after canonical markdown changes."

> **Dev:** "Can we show Vale-Pedagio as part of the driver's safe money?"
> **Domain expert:** "No. Vale-Pedagio is **Pass-Through Cash Flow**. It can appear in cash-flow visibility, but it is excluded from **Safe Personal Withdrawal**."

> **Dev:** "Can Cofrete say a freight is officially legal because the checker says above_floor?"
> **Domain expert:** "No. A **Freight-Floor Check** is advisory. It must show source period and confidence, and users must confirm official questions with ANTT or qualified professionals."

> **Dev:** "Does marking a receivable paid mean Cofrete collected the money?"
> **Domain expert:** "No. **Customer Payment Tracking** records driver-entered cash-flow status only. It is not payment processing or collection."

## Flagged ambiguities

- "docs" was used to describe both canonical markdown files and generated HTML summaries. Resolved: markdown docs are canonical; HTML explainers are manually maintained derived summaries until a generator exists.
- "wallet" can imply a real payment account. Resolved: **Reserve Wallet** is a virtual planning ledger until a future payment integration explicitly implements real money movement.
- "safe withdrawal" can imply guaranteed availability. Resolved: **Safe Personal Withdrawal** is advisory planning output, not legal, fiscal, operational, or cash-availability confirmation.
- "freight floor checker" can imply official ANTT validation. Resolved: **Freight-Floor Check** is advisory and may return `unknown`; it is not enforcement or legal certification.
- "customer" can mean Cofrete's own user account or a payer on a freight. Resolved: **Freight Customer** is the payer tracked by the driver for receivables and customer profitability.
