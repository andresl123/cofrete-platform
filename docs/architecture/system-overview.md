# System Overview

Cofrete is a monorepo with a mobile-first product surface, a Core API, asynchronous workers, and durable documentation.

```text
mobile-app / web-app
        |
        v
     core-api  ---- PostgreSQL
        |
        v
     RabbitMQ
        |
        +--> finance-worker
        +--> data-importer-worker
```

## Responsibilities

- `mobile-app` presents driver workflows and calls Cofrete APIs only.
- `web-app` supports admin, demo, and backoffice workflows.
- `core-api` owns synchronous product APIs and primary business data.
- `finance-worker` owns deterministic asynchronous calculations.
- `data-importer-worker` imports and normalizes external datasets.
- `docs` owns product, architecture, compliance, runbook, and harness knowledge.

## Design Constraints

- Financial calculations must be deterministic and auditable.
- External ANP/ANTT data should be imported server-side, not fetched by clients.
- Compliance wording must remain advisory and link to official sources.
- Events must include correlation IDs for tracing.
- Contracts live in docs until generated or shared contract packages are introduced.
- `core-api` must be internally modular from the first implementation, with explicit `profile`, `trip`, `finance`, `reserve`, `compliance`, `receivables`, and `imports` modules so domains can be tested independently and extracted later if scale requires it.
