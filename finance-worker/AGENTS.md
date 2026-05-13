# Finance Worker Agent Guide

Owns finance and billing background processing. Keep worker code, tests, and local configuration in this directory unless a task explicitly introduces shared contracts or documentation.

## Deterministic Finance Boundary

This service may consume and publish canonical finance events, but calculation algorithms are intentionally out of scope for the scaffold.

- `trip.recalculation.requested` listener stubs must not calculate profitability until the deterministic finance engine issue lands.
- `reserve.allocation.requested` listener stubs must not allocate reserves until the reserve allocation issue lands.
- `trip.finance.recalculated` publisher stubs must preserve the canonical event name from `../docs/architecture/event-contracts.md`.
- Monetary values in event payloads remain decimal strings with explicit `BRL` currency unless a later contract change updates the architecture docs first.
- Toll reimbursement and Vale-Pedagio are pass-through money and must not be counted as profit.
- Finance output is advisory product software, not official legal, tax, accounting, insurance, or government guidance.

## Validation

Run service validation from this directory:

```sh
mvn -q validate test
```

Run the root harness check after repository-structure changes:

```sh
python ../scripts/agent_harness_check.py
```
