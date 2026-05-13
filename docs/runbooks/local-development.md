# Local Development Runbook

## Current State

Repository foundation, docs, CI guardrails, and local Docker Compose infrastructure exist. Service-specific commands become active as scaffolding issues land.

## Baseline Check

```sh
python scripts/agent_harness_check.py
```

## Local Infrastructure

Docker Compose provides the disposable local stack shared by backend and worker development:

| Service | Container | Compose DNS name | Host port | Internal port | Purpose |
|---|---|---|---:|---:|---|
| PostgreSQL | `cofrete-postgres` | `postgres` | `5432` | `5432` | Application database for local backend and worker scaffolds. |
| RabbitMQ | `cofrete-rabbitmq` | `rabbitmq` | `5672` | `5672` | AMQP broker for local events and worker queues. |
| RabbitMQ Management | `cofrete-rabbitmq` | `rabbitmq` | `15672` | `15672` | Browser management UI for local inspection. |
| MinIO API | `cofrete-minio` | `minio` | `9000` | `9000` | S3-compatible object storage for local documents/imports. |
| MinIO Console | `cofrete-minio` | `minio` | `9001` | `9001` | Browser console for local bucket inspection. |

The shared Compose network is `cofrete-local`. Named volumes are `cofrete-postgres-data`, `cofrete-rabbitmq-data`, and `cofrete-minio-data`.

Start the stack:

```sh
cp .env.example .env
docker compose up -d
```

Validate the Compose model without starting containers:

```sh
docker compose config
```

Check health:

```sh
docker compose ps
docker compose logs postgres
docker compose logs rabbitmq
docker compose logs minio
docker compose logs minio-init
```

Reset disposable local data:

```sh
docker compose down -v
docker compose up -d
```

Only synthetic local data belongs in this stack. Do not import production credentials, production user data, or unverified government/insurer datasets. Demo fixtures must not imply official ANP, ANTT, SUSEP, Receita Federal, or insurer validation unless the source is explicitly documented.

## Service Connection Placeholders

Use internal service names when a backend container joins the Compose network:

| Service | PostgreSQL | RabbitMQ | Object storage |
|---|---|---|---|
| `core-api` | `jdbc:postgresql://postgres:5432/cofrete_local` | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `http://minio:9000`, bucket `cofrete-local` |
| `finance-worker` | `jdbc:postgresql://postgres:5432/cofrete_local` | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | Not required until a worker use case is documented. |
| `data-importer-worker` | `jdbc:postgresql://postgres:5432/cofrete_local` | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `http://minio:9000`, bucket `cofrete-local` |

Use localhost ports when running a service directly on the host:

| Dependency | Host connection |
|---|---|
| PostgreSQL | `localhost:5432`, database `cofrete_local` |
| RabbitMQ | `localhost:5672`, vhost `cofrete` |
| RabbitMQ Management | `http://localhost:15672` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |

The `.env.example` file contains the same placeholders with safe local defaults, including `COFRETE_CORE_API_DATABASE_URL`, worker AMQP URLs, and MinIO bucket settings. Copy to `.env` for personal overrides; `.env` is ignored by Git.

## Expected Future Flow

1. Start local infrastructure with Docker Compose.
2. Run Core API against local PostgreSQL.
3. Run workers against local RabbitMQ.
4. Run mobile/web clients against local Core API.
5. Use demo seed for end-to-end validation.

## Docker Compose And Kubernetes Boundary

Docker Compose is for local development only. It should mirror Kubernetes service names, ports, environment variable names, and health expectations where practical, but it is not the production runtime.

Use Compose for:

- Local PostgreSQL, RabbitMQ, and MinIO.
- Fast backend/worker development.
- Fixture imports and local smoke flows.
- Safe reset of disposable development data.

Use Kubernetes for:

- Staging and production deployment.
- Ingress/TLS behavior.
- Kubernetes Secrets and ConfigMaps.
- Rollout, rollback, probes, and resource checks.
- Production-like smoke validation.

When COF-003 adds Compose, it should choose names and ports that make later Kubernetes manifests predictable. It should not imply that production stateful dependencies will be unmanaged Compose-style containers.

## Kubernetes Access Status

As of 2026-05-11, this workspace could not inspect the real cluster: the Kubernetes gateway URL was not configured, and `kubectl` did not have an authenticated current context. See `docs/architecture/kubernetes.md` for the cluster discovery checklist.

## Troubleshooting

- If harness fails, add the missing doc, service ownership file, or required text marker.
- If `docker compose config` fails, check for invalid edits in `docker-compose.yml` or malformed values in `.env`.
- If a port is already in use, change the matching host port in `.env`, such as `COFRETE_POSTGRES_PORT`, `COFRETE_RABBITMQ_MANAGEMENT_PORT`, `COFRETE_MINIO_API_PORT`, or `COFRETE_MINIO_CONSOLE_PORT`.
- If a container remains unhealthy, inspect `docker compose logs <service>` and verify Docker has enough memory and disk space.
- If MinIO bucket creation fails, rerun `docker compose up minio-init` after `minio` is healthy.
- If local state is stale or corrupted, run `docker compose down -v` and start again with synthetic data only.
