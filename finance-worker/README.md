# Finance Worker

Background worker for finance-related processing in Cofrete Platform.

## Scope

Finance Worker owns asynchronous finance recalculation, reserve allocation, safe-withdrawal calculation, and financial health scoring tasks.

The service includes the ROU-216 deterministic trip finance calculation engine. ROU-217 owns virtual reserve allocation behavior after freight payment or reserve-rule changes.

Finance output is advisory product software. Do not present worker results as official ANTT, ANP, SUSEP, Receita Federal, DETRAN, SEFAZ, insurer, legal, tax, accounting, or government authority.

## Event Contracts

Canonical event names must match `../docs/architecture/event-contracts.md` exactly.

Consumed stubs:

- `trip.recalculation.requested`
- `reserve.allocation.requested`

Published stub:

- `trip.finance.recalculated`

`trip.recalculation.requested` handling validates the canonical event, deduplicates by idempotency key, loads an explicit trip finance input snapshot, calculates deterministic money output, and publishes `trip.finance.recalculated`.

The current event contract carries trip IDs and `inputRevision`, not the full calculation snapshot. `TripFinanceInputSnapshotProvider` is the integration boundary for a future Core API or durable read-model source. The default provider fails explicitly so the worker does not fabricate production finance results before authoritative inputs exist.

Calculation rules, reserve formulas, rounding, and trace output are documented in `../docs/architecture/finance-calculation-engine.md`.

## Local Development

Start RabbitMQ from the repository root:

```sh
docker compose up -d rabbitmq
```

Run the worker from this directory:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The local profile defaults to the RabbitMQ instance exposed by the root Docker Compose stack:

```text
amqp://cofrete:cofrete_local_password@localhost:5672/cofrete
```

Use `COFRETE_FINANCE_WORKER_AMQP_URL` to override the connection. When the worker runs inside the Compose network, use `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete`.

To run the worker container with Compose after building the image:

```sh
docker compose --profile services up finance-worker
```

## Health

The scaffold exposes Spring Boot Actuator health:

```http
GET /actuator/health
```

The default local HTTP port is `8081`.

## Validation

Run service validation from this directory:

```sh
mvn -q validate test
```

Run repository validation from the root:

```sh
docker compose config
python scripts/agent_harness_check.py
```

## Local Infrastructure Placeholders

Use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |

The matching placeholder variables are `COFRETE_FINANCE_WORKER_DATABASE_URL`, `COFRETE_FINANCE_WORKER_AMQP_URL`, and `COFRETE_FINANCE_WORKER_PORT` in the root `.env.example`.
