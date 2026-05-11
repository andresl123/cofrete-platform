# Agent Guide

This file is intentionally short. Durable project knowledge belongs in versioned docs so agents can find, cite, test, and update it.

## Start Here

1. Read `README.md`.
2. Read `docs/agent-harness/README.md`.
3. Read the nearest service `AGENTS.md` before editing service code.
4. Run `python scripts/agent_harness_check.py` before opening a PR or handing work back.

## Repo Layout

- `core-api/`: primary backend API service.
- `finance-worker/`: finance and billing background processing.
- `data-importer-worker/`: data ingestion and import processing.
- `mobile-app/`: mobile client application.
- `web-app/`: web client application.
- `docs/`: product, architecture, compliance, and operational documentation.
- `scripts/`: local automation and validation helpers.

## Harness Docs

| Topic | Source of truth |
|---|---|
| Agent workflow | `docs/agent-harness/workflow.md` |
| System boundaries | `docs/architecture/service-boundaries.md` |
| API contracts | `docs/architecture/api-contracts.md` |
| Event contracts | `docs/architecture/event-contracts.md` |
| Validation | `docs/agent-harness/validation.md` |
| Testing policy | `docs/agent-harness/testing-policy.md` |
| Golden principles | `docs/agent-harness/golden-principles.md` |
| Risk register | `docs/agent-harness/risk-register.md` |

## Working Rules

- Use a dedicated Git worktree branch for each task.
- Read the nearest `AGENTS.md` before editing a service.
- Keep service-specific changes inside that service unless the task requires shared repo updates.
- Run `python scripts/agent_harness_check.py` before opening a PR that changes repository structure.
- Do not commit secrets, local environment files, dependency caches, or build artifacts.
- Update docs when architecture, contracts, validation commands, compliance wording, or risk boundaries change.
- Finance math must be deterministic, auditable, and tested with exact expected values.
- Toll reimbursement and Vale-Pedagio are pass-through money and must not be counted as profit.
- Cofrete is not an official government, legal, tax, accounting, or insurance channel.

## Ownership

Each service directory owns its application code, tests, service documentation, and local build configuration. Cross-service contracts and system-wide decisions belong in `docs/` until a shared package or contract directory is introduced.
