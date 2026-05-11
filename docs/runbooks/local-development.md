# Local Development Runbook

## Current State

Only repository foundation and docs exist. Service-specific commands become active as scaffolding issues land.

## Baseline Check

```sh
python scripts/agent_harness_check.py
```

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
- If local infrastructure is missing, complete COF-003 before running service scaffolds.
