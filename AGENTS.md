# Agent Guide

This file is intentionally short. Durable project knowledge belongs in versioned docs so agents can find, cite, test, and update it.

## Start Here

1. Read `README.md`.
2. Read `CONTEXT.md`.
3. Read `docs/agent-harness/README.md`.
4. Read the nearest service `AGENTS.md` before editing service code.
5. Run `python scripts/agent_harness_check.py` before opening a PR or handing work back.

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
| Runtime smoke tests | `docs/agent-harness/runtime-smoke-tests.md` |
| Validation | `docs/agent-harness/validation.md` |
| Frontend review loop | `docs/agent-harness/frontend-review-loop.md` |
| Linear review loop | `docs/agent-harness/linear-review-loop.md` |
| Testing policy | `docs/agent-harness/testing-policy.md` |
| Golden principles | `docs/agent-harness/golden-principles.md` |
| Risk register | `docs/agent-harness/risk-register.md` |

## Working Rules

- Before making any file change, create and switch to a dedicated Git worktree branch for the task.
- Do not start implementation edits in the main checkout. First create the task worktree, then make changes inside that worktree.
- Prefer `scripts/start_task_worktree.sh <task-name> [base-branch]` when starting a new task worktree.
- always make sure you have the newest data from origin/main
- For every new task, invoke the `caveman` skill in `lite` mode and keep responses concise, professional, and technically complete unless the user asks for normal mode.
- For any frontend interface change, invoke the `impeccable` skill before editing UI files for required context loading, register selection, and design preflight.
- At the end of any task that changes frontend or mobile files, run the Impeccable review loop described in `docs/agent-harness/frontend-review-loop.md`.
- Read the nearest `AGENTS.md` before editing a service.
- Keep service-specific changes inside that service unless the task requires shared repo updates.
- Run `python scripts/agent_harness_check.py` before opening a PR that changes repository structure.
- Do not commit, push, open a PR, or update an existing PR until the user has reviewed the local changes and confirmed validation is complete.
- Do not commit secrets, local environment files, dependency caches, or build artifacts.
- Update docs when architecture, contracts, validation commands, compliance wording, or risk boundaries change.
- At the end of every task, create or update a task-specific HTML explainer under `docs/explainers/`. Explainers summarize what changed, why it changed, affected files or areas, validation run, and the next task it unblocks when relevant. They are derived handoff aids, not source-of-truth docs.
- After implementing a Linear issue, run the `grill-with-docs` review-and-fix loop described in `docs/agent-harness/linear-review-loop.md` before final handoff.
- Finance math must be deterministic, auditable, and tested with exact expected values.
- Toll reimbursement and Vale-Pedagio are pass-through money and must not be counted as profit.
- Cofrete is not an official government, legal, tax, accounting, or insurance channel.

## Ownership

Each service directory owns its application code, tests, service documentation, and local build configuration. Cross-service contracts and system-wide decisions belong in `docs/` until a shared package or contract directory is introduced.
