# Deployment

Kubernetes is the intended production runtime for Cofrete. Local Docker Compose remains a developer convenience for PostgreSQL, RabbitMQ, MinIO, and service networking, but Compose is not the production deployment model.

Detailed Kubernetes conventions live in `docs/architecture/kubernetes.md`.

## Target Environments

| Environment | Runtime | Purpose |
|---|---|---|
| Local | Docker Compose plus local service processes | Fast developer feedback and fixture validation |
| CI | GitHub Actions | Harness checks, tests, image build validation, and future manifest validation |
| Staging | User Kubernetes cluster | Production-like release candidate validation |
| Production | User Kubernetes cluster | Live Cofrete API, workers, web/admin surface, and mobile API backend |

## Production Runtime Shape

Application workloads expected in Kubernetes:

- `core-api` as a Deployment and ClusterIP Service.
- `finance-worker` as a Deployment consuming RabbitMQ events.
- `data-importer-worker` as a Deployment and, later, CronJobs or scheduled jobs for imports.
- `web-app` as a Deployment and ClusterIP Service when the web/admin dashboard is enabled.
- Ingress for externally reachable HTTP services.

Stateful dependencies require an explicit decision before production:

| Dependency | Preferred Production Shape | Notes |
|---|---|---|
| PostgreSQL | Existing managed/external database or verified in-cluster operator | Do not run production data without backup and restore proof. |
| RabbitMQ | Existing managed/external broker or verified in-cluster operator | Must expose queue depth and dead-letter visibility. |
| Object storage | Existing S3-compatible service, managed bucket, or verified MinIO deployment | Used for documents, receipts, and insurance files. |
| Container registry | GitHub Container Registry or existing private registry | CI must push immutable image tags. |
| TLS | Cluster ingress controller plus certificate automation | Confirm actual controller and issuer before manifests. |
| Secrets | Kubernetes Secrets backed by the cluster's secret-management convention | Secret values must not be committed. |

## Cluster Discovery Status

Real cluster inspection is pending.

Attempted on 2026-05-11:

- Kubernetes REST gateway: token alias was present, but no gateway URL was configured, so `/healthz` and discovery endpoints could not be called.
- `kubectl`: installed, but no current context was set and API calls required credentials.

Until discovery succeeds, docs and manifests must mark cluster-specific values as proposed defaults rather than confirmed facts.

## Proposed Defaults Pending Discovery

These defaults are safe starting points but must be validated against the real cluster:

| Topic | Proposed Default |
|---|---|
| Namespace | `cofrete` for production, `cofrete-staging` for staging |
| App labels | `app.kubernetes.io/name`, `app.kubernetes.io/component`, `app.kubernetes.io/part-of=cofrete` |
| Container ports | `8080` for Java services, web port decided by scaffold |
| Probes | `/actuator/health/liveness` and `/actuator/health/readiness` for Spring services |
| Resources | Requests and limits required before production deploy |
| Rollout | RollingUpdate with at least two replicas for HTTP services when cluster capacity allows |
| Config | ConfigMaps for non-secret environment, Secrets for credentials |
| Network | Default-deny NetworkPolicies after service ports are known |

## Release Rules

- Run repository harness checks before every PR.
- Run service-specific tests for changed services.
- Build immutable container images for deployable services.
- Validate Kubernetes manifests before deployment.
- Confirm image tags, ConfigMaps, Secrets, Services, Ingress, probes, and rollout status in staging before production.
- Review compliance wording for changes touching RNTRC, CIOT, Vale-Pedagio, ANP, insurance, MEI, taxes, or documents.
- Verify demo seed and smoke flow before MVP release.

## Config Rules

- Production secrets must not live in Git.
- Local `.env.example` must use safe placeholders only.
- Kubernetes manifests must reference secret names and keys, not secret values.
- Data import jobs should be feature-flagged or schedulable.
- Mobile and web API URLs must be environment-specific and documented.

## Follow-Up Implementation Issues

- COF-003 should keep Docker Compose local-only and align service names, ports, and env vars with Kubernetes conventions.
- COF-004 should validate harness, Compose config, container build metadata, and future manifest rendering.
- Add a future Kubernetes manifests issue for Helm or Kustomize once service scaffolds exist.
- Add a future release automation issue for image publish, staging deploy, smoke checks, and production promotion.
