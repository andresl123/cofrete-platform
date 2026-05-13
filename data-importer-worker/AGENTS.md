# Data Importer Worker Agent Guide

Owns data ingestion and import processing. Keep importer code, tests, and local configuration in this directory unless a task explicitly introduces shared contracts or documentation.

## Scope Rules

- Fixtures must be synthetic or clearly sourced in the fixture file and README.
- Import jobs must expose source, freshness, confidence, import audit ID, and correlation ID.
- Event publishers must use the canonical names in `../docs/architecture/event-contracts.md` exactly.
- Do not add production paid toll provider integration without a dedicated issue and compliance review.

## Validation

Run service tests after worker changes:

```sh
mvn -q validate test
```

Run the root harness check after repository-structure changes:

```sh
python ../scripts/agent_harness_check.py
```
