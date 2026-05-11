# Kubernetes Architecture

This document makes the user's Kubernetes cluster the production deployment target for Cofrete. It records confirmed facts, proposed defaults, and open decisions so service scaffolding does not accidentally optimize for Docker Compose as production.

## Current Cluster Access Status

Real cluster inspection is not currently available from this workspace.

Observed on 2026-05-11:

- `KUBE_GATEWAT_TOKEN` is present as an environment variable name, but no Kubernetes REST gateway URL environment variable is configured.
- `kubectl config current-context` reports no current context.
- `kubectl get namespaces` and `kubectl get nodes` fail because the API server asks for credentials.

No cluster namespaces, ingress classes, storage classes, node sizes, installed operators, registry credentials, certificate issuers, or observability components have been confirmed. Do not commit specific values for those until discovery succeeds.

## Discovery Checklist

When gateway or kubeconfig access is available, collect these facts without exposing secrets:

```sh
python ~/.codex/skills/kube-rest-gateway/scripts/gateway_query.py /healthz
python ~/.codex/skills/kube-rest-gateway/scripts/gateway_query.py /api/help
python ~/.codex/skills/kube-rest-gateway/scripts/gateway_query.py /api/namespaces
```

If using `kubectl`, collect:

```sh
kubectl config current-context
kubectl get namespaces
kubectl get nodes
kubectl get storageclass
kubectl get ingressclass
kubectl get deployments --all-namespaces
kubectl get services --all-namespaces
kubectl get ingresses --all-namespaces
```

Record only infrastructure shape and names needed for deployment. Do not commit tokens, secret values, private endpoint credentials, or sensitive workload data.

## Namespace Strategy

Proposed defaults:

- `cofrete-staging`: staging/release-candidate validation.
- `cofrete`: production.

Each namespace should contain only Cofrete workloads and namespaced configuration. Shared cluster services such as ingress controllers, cert managers, observability stacks, or external secret operators should remain in their existing cluster namespaces.

## Workload Inventory

| Component | Kubernetes Kind | External Exposure | Notes |
|---|---|---|---|
| `core-api` | Deployment, Service | Ingress/API route | Primary backend API. |
| `finance-worker` | Deployment | None | Consumes finance events from RabbitMQ. |
| `data-importer-worker` | Deployment, later CronJob | None | Imports ANP/toll datasets and publishes import events. |
| `web-app` | Deployment, Service | Ingress route when enabled | Admin/backoffice UI. |
| `mobile-app` | Not deployed as a cluster workload | App-store/client artifact | Uses environment-specific API base URL. |

## Stateful Dependency Decision

Production application workloads should run in Kubernetes, but stateful dependencies require explicit reliability decisions.

| Dependency | Production Decision | Reason |
|---|---|---|
| PostgreSQL | Prefer managed/external or existing in-cluster operator with backups | Financial, compliance, and document metadata must survive node loss. |
| RabbitMQ | Prefer managed/external or existing in-cluster operator with DLQ visibility | Finance/import processing depends on reliable queues. |
| Object storage | Prefer S3-compatible external service or verified MinIO with backups | Stores receipts, documents, and insurance files. |
| Container registry | GitHub Container Registry unless user has a preferred registry | Keeps image publishing close to GitHub Actions. |

Do not deploy production PostgreSQL, RabbitMQ, or MinIO as plain unmanaged StatefulSets unless backup, restore, storage class, resource limits, and operational ownership are documented.

## Image And Tagging Convention

Proposed image names:

```text
ghcr.io/andresl123/cofrete-core-api:<git-sha>
ghcr.io/andresl123/cofrete-finance-worker:<git-sha>
ghcr.io/andresl123/cofrete-data-importer-worker:<git-sha>
ghcr.io/andresl123/cofrete-web-app:<git-sha>
```

Rules:

- CI publishes immutable SHA tags.
- Mutable tags such as `latest` are not used for production rollout decisions.
- Deployments reference exact image tags.
- Rollback uses the previous known-good image tag and Kubernetes rollout history.

## Configuration And Secrets

ConfigMaps may hold non-secret values:

- service ports.
- profile names.
- feature flags.
- public API base URL.
- import schedule flags.

Kubernetes Secrets or the cluster's existing secret-management integration must hold:

- database credentials.
- RabbitMQ credentials.
- object storage keys.
- JWT/signing secrets.
- internal service credentials.
- external API tokens, if added later.

Secret values must never be committed, logged, or written into Linear/GitHub comments.

## Networking And Ingress

Required before production:

- Confirm ingress controller and `IngressClass`.
- Confirm TLS certificate mechanism.
- Confirm public hostnames for API and web/admin.
- Decide whether API and web share a hostname with path routing or use separate hostnames.
- Restrict worker services from external exposure.

Proposed exposure:

| Surface | Exposure |
|---|---|
| Core API | HTTPS ingress |
| Web admin | HTTPS ingress with admin auth when enabled |
| Workers | No ingress |
| PostgreSQL/RabbitMQ/Object storage | No public exposure from Cofrete manifests |

## Health And Readiness

Spring Boot services should expose:

- liveness: `/actuator/health/liveness`.
- readiness: `/actuator/health/readiness`.

Deployment expectations:

- Readiness must fail when required dependencies are unavailable.
- Liveness must not fail for transient downstream dependency outages.
- Workers must expose a health endpoint or equivalent probe before production deployment.
- Startup probes should be considered if JVM startup time is slow.

## Resource And Scaling Rules

Each Deployment must define resource requests and limits before production:

- CPU request/limit.
- Memory request/limit.
- JVM container memory settings for Java services.

Initial replicas:

- `core-api`: 2 replicas when database and cluster capacity allow.
- `web-app`: 2 replicas when enabled.
- `finance-worker`: 1 replica until idempotency and concurrency are proven.
- `data-importer-worker`: 1 replica until job locking is implemented.

Horizontal scaling is not a substitute for idempotency. Finance and importer workers must tolerate retries before multiple replicas are enabled.

## Persistence And Backups

Before production launch, document:

- PostgreSQL backup schedule.
- PostgreSQL restore test.
- object storage backup/retention.
- RabbitMQ durability and DLQ retention.
- PVC storage class if any stateful service runs in-cluster.

Financial data, document metadata, insurance records, and compliance metadata are high-risk data. A deployment is not production-ready without restore proof.

## Observability

Minimum deployment signals:

- Kubernetes rollout status.
- Pod restarts and readiness state.
- Structured application logs with correlation IDs.
- Finance calculation failures.
- Import failures and stale dataset alerts.
- RabbitMQ queue depth and dead-letter count.
- Database connectivity errors.

Prometheus/Grafana, Loki, OpenTelemetry, or another stack may be used, but the real cluster stack is not confirmed yet.

## Network Policy

Target policy after services and ports are known:

- Default deny ingress for Cofrete namespaces.
- Allow ingress controller to reach `core-api` and `web-app`.
- Allow workers to reach RabbitMQ, PostgreSQL, and object storage as needed.
- Allow `core-api` to reach PostgreSQL, RabbitMQ, and object storage as needed.
- Deny public access to worker workloads and stateful dependencies.

## CI/CD Implications

GitHub Actions should eventually:

1. Run harness and service tests.
2. Build service images.
3. Push immutable SHA-tagged images.
4. Render Kubernetes manifests.
5. Validate manifests with a schema/linting tool.
6. Deploy to staging.
7. Run smoke checks against staging.
8. Promote the same image tags to production after approval.

COF-004 should wire only the checks that are possible at that point. Manifest rendering and deployment automation should wait until service scaffolds and deployment tooling exist.

## Open Decisions

- Confirm gateway URL or kubeconfig access path.
- Confirm namespace names.
- Confirm ingress controller and TLS issuer.
- Confirm registry choice.
- Confirm whether PostgreSQL, RabbitMQ, and object storage are external or in-cluster.
- Confirm storage classes and backup tooling.
- Confirm observability stack.
- Confirm domain names for API and web/admin.
- Confirm whether mobile app points to staging/production via build-time config or runtime environment.
