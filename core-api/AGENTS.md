# Core API Agent Guide

Owns the backend API service. Keep API changes, service tests, and local backend configuration in this directory unless a task explicitly introduces shared contracts or documentation.

## Validation

Run service checks from this directory:

```sh
mvn -q validate test
```

Run the root harness check after repository-structure changes:

```sh
python ../scripts/agent_harness_check.py
```

Validate Compose wiring from the repository root when local infrastructure or datasource configuration changes:

```sh
docker compose config
```

## Ownership Boundaries

Core API owns authenticated product APIs and primary PostgreSQL data models for profile, trip, advisory finance, reserve, compliance, receivables, imports, auth, and audit modules.

Keep the service internally modular:

- `profile`: drivers, trucks, trailers, and tax profiles.
- `trip`: freights, trips, direct costs, toll records, and profitability snapshots.
- `finance`: advisory product finance data and worker result surfaces.
- `reserve`: reserve rules, wallets, allocations, and transactions.
- `compliance`: advisory compliance metadata, documents, calendars, and official-source links.
- `receivables`: customers, receivables, payments, and customer profitability.
- `imports`: imported dataset status and import audit records.
- `auth`: Cofrete application identity, sessions, authorization, and Spring Security.
- `audit`: user, admin, support, and internal-service audit logging.

Cross-module behavior should go through explicit application/service interfaces. Do not wire controllers directly to another module's persistence or private domain logic.

Do not implement finance or compliance behavior directly in controllers without matching contract documentation. Cofrete is advisory software and must not imply official government, legal, tax, accounting, insurance, ANTT, ANP, SUSEP, Receita Federal, DETRAN, or SEFAZ authority.
