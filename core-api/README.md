# Core API

Backend API service for Cofrete Platform.

## Scope

Core API owns authenticated product APIs and primary PostgreSQL persistence for driver profile, trip, advisory finance, reserve, compliance, receivables, imports, auth, and audit data.

The service now implements the first profile-domain product controllers plus reserve wallet/allocation endpoints. Remaining contracted routes under `/api/*` are reserved for implementation issues documented in `../docs/architecture/api-contracts.md`.

Reserve allocation is async: Core API records allocation requests, publishes `reserve.allocation.requested`, and idempotently persists Finance Worker `reserve.allocation.completed` results into reserve wallets and transactions.

Compliance and finance behavior must remain advisory. Do not present Core API responses as official ANTT, ANP, SUSEP, Receita Federal, DETRAN, SEFAZ, insurer, legal, tax, accounting, or government authority.

## Local Development

Start the repository infrastructure first:

```sh
docker compose up -d postgres
```

Run the service from this directory:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The local profile defaults to the PostgreSQL instance exposed by the root Docker Compose stack:

```text
jdbc:postgresql://localhost:5432/cofrete_local
```

Use `COFRETE_CORE_API_DATABASE_URL`, `COFRETE_POSTGRES_USER`, and `COFRETE_POSTGRES_PASSWORD` to override the connection. When the service runs inside the Compose network, use `jdbc:postgresql://postgres:5432/cofrete_local`.

## Health

The scaffold exposes Spring Boot Actuator health:

```http
GET /actuator/health
```

Only the health endpoint is public. Product APIs under `/api/*` are reserved for authenticated Cofrete principals.

## Profile Domain

ROU-213 / COF-009 implements the first authenticated Core API profile model:

- `POST /api/drivers`, `GET /api/drivers/me`, and `PATCH /api/drivers/me`
- `POST /api/trucks`, `GET /api/trucks`, `GET /api/trucks/{truckId}`, and `PUT /api/trucks/{truckId}`
- `POST /api/tax-profile` and `GET /api/tax-profile`

The backing migration creates app identity, driver, truck, trailer, tax profile, tax rule year, IPVA rule, compliance profile, and insurance policy metadata tables. CPF/CNPJ, RNTRC, RENAVAM, and plate identifiers are treated as app-maintained metadata and masked in normal responses.

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

## Internal Modules

Keep cross-module behavior behind application/service interfaces. Do not share persistence or business logic ad hoc across modules.

Current package boundaries:

- `com.cofrete.coreapi.profile`
- `com.cofrete.coreapi.trip`
- `com.cofrete.coreapi.finance`
- `com.cofrete.coreapi.reserve`
- `com.cofrete.coreapi.compliance`
- `com.cofrete.coreapi.receivables`
- `com.cofrete.coreapi.imports`
- `com.cofrete.coreapi.auth`
- `com.cofrete.coreapi.audit`

## Local Infrastructure Placeholders

Use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |
| MinIO | `http://minio:9000`, bucket `cofrete-local` | `http://localhost:9000`, bucket `cofrete-local` |

The matching placeholder variables are `COFRETE_CORE_API_DATABASE_URL`, `COFRETE_CORE_API_AMQP_URL`, `COFRETE_CORE_API_OBJECT_STORAGE_ENDPOINT`, and `COFRETE_CORE_API_OBJECT_STORAGE_BUCKET` in the root `.env.example`.
