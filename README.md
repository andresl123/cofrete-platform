# Cofrete Platform

Cofrete Platform is a multi-service repository for the Cofrete product. The repository is intentionally structured around service boundaries so API, worker, mobile, web, and documentation work can land in focused PRs.

## Structure

- `core-api/`: backend API.
- `finance-worker/`: finance background worker.
- `data-importer-worker/`: data import worker.
- `mobile-app/`: mobile application.
- `web-app/`: web application.
- `docs/`: product, architecture, compliance, and operations docs.
- `scripts/`: local automation.

## Validation

Run the repository harness check from the repo root:

```sh
python scripts/agent_harness_check.py
```

This verifies the baseline folders and ownership files expected by follow-up tasks.
