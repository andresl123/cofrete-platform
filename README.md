# Cofrete Platform

Cofrete is a financial health and compliance assistant for Brazilian truck drivers and owner-operators. It helps answer one practical question:

> After fuel, tolls, maintenance, tires, insurance, taxes, documents, emergency reserve, and future truck replacement, how much money is actually safe to use?

This repository is structured as a multi-service platform so API, worker, mobile, web, and documentation work can land in focused PRs.

## Structure

- `core-api/`: backend API.
- `finance-worker/`: finance background worker.
- `data-importer-worker/`: data import worker.
- `mobile-app/`: mobile application.
- `web-app/`: web application.
- `docs/`: product, architecture, compliance, and operations docs.
- `scripts/`: local automation.

## Source Of Truth

- Product scope: `docs/product/mvp-scope.md`
- Product promise: `docs/product/brazil-trucker-finance-blueprint.md`
- Architecture: `docs/architecture/system-overview.md`
- Kubernetes deployment: `docs/architecture/kubernetes.md`
- Repository structure: `docs/architecture/repository-structure.md`
- Service ownership: `docs/architecture/service-boundaries.md`
- API contracts: `docs/architecture/api-contracts.md`
- Event contracts: `docs/architecture/event-contracts.md`
- Compliance boundaries: `docs/compliance/official-source-register.md`
- Agent workflow: `docs/agent-harness/README.md`

## Compliance Boundary

Cofrete is advisory product software. It is not an official government, legal, tax, accounting, or insurance channel. Production behavior must link drivers to official ANTT, ANP, SUSEP, Receita Federal, state DETRAN/SEFAZ, insurer, and professional guidance where appropriate.

## Validation

Run the repository harness check from the repo root:

```sh
python scripts/agent_harness_check.py
```

This verifies the baseline folders, ownership files, and required documentation expected by follow-up tasks.

## Current Status

Working now:

- Repository foundation and service ownership placeholders.
- Product, architecture, compliance, runbook, and agent harness docs.
- Harness validation script.
- Worktree helper script for task branches.

Planned in follow-up issues:

- Docker Compose local infrastructure and `.env.example`.
- GitHub PR checks, PR template, and image publishing workflow.
- Java/Spring Boot service scaffolds.
- Expo mobile app scaffold.
- React/Vite web app scaffold.
- Runtime smoke scripts and demo seed/reset scripts.
