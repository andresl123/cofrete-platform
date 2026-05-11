# Agent Guide

This repository uses small, scoped agent tasks. Keep changes narrow, validate before handoff, and avoid modifying unrelated work.

## Repo Layout

- `core-api/`: primary backend API service.
- `finance-worker/`: finance and billing background processing.
- `data-importer-worker/`: data ingestion and import processing.
- `mobile-app/`: mobile client application.
- `web-app/`: web client application.
- `docs/`: product, architecture, compliance, and operational documentation.
- `scripts/`: local automation and validation helpers.

## Working Rules

- Use a dedicated Git worktree branch for each task.
- Read the nearest `AGENTS.md` before editing a service.
- Keep service-specific changes inside that service unless the task requires shared repo updates.
- Run `python scripts/agent_harness_check.py` before opening a PR that changes repository structure.
- Do not commit secrets, local environment files, dependency caches, or build artifacts.

## Ownership

Each service directory owns its application code, tests, service documentation, and local build configuration. Cross-service contracts and system-wide decisions belong in `docs/` until a shared package or contract directory is introduced.
