# Production Release Runbook

## Pre-Release Checklist

- Repository harness passes.
- Changed services pass their validation commands.
- Demo seed and smoke flow pass.
- MVP launch runbook checklist passes when releasing the MVP surface.
- Kubernetes manifests render and validate for the target environment.
- Container images are immutable SHA tags and were built by CI.
- Staging rollout is healthy before production promotion.
- Kubernetes Secrets and ConfigMaps exist in the target namespace.
- Readiness and liveness probes are configured for deployed services.
- Compliance source register reviewed.
- Risk register reviewed.
- Finance calculations have deterministic tests.
- User-facing compliance wording remains advisory.
- No real secrets or production credentials are committed.

## Kubernetes Release Gates

Before a production rollout:

1. Confirm the target namespace.
2. Confirm image tags for every changed workload.
3. Confirm database, RabbitMQ, and object storage endpoints are reachable from staging.
4. Confirm no Secret values are present in rendered manifests or logs.
5. Apply or sync manifests to staging.
6. Verify rollout status for each Deployment.
7. Run staging smoke checks.
8. Promote the same image tags to production after approval.
9. Verify production rollout status and smoke checks.

Rollback should use the previous known-good image tag and Kubernetes rollout history. If data migrations are involved, rollback must include a migration rollback or forward-fix plan.

## Compliance Review

Review docs and UI copy touching:

- RNTRC/ANTT.
- CIOT.
- Vale-Pedagio and tolls.
- ANP diesel data.
- SUSEP/insurance.
- MEI Caminhoneiro.
- IPVA/licensing.
- Tax profiles.

For MVP launch, also run the source checklist in `docs/runbooks/mvp-launch.md`.

## Release Decision

Do not release if the app presents itself as an official government, legal, tax, accounting, or insurance channel.

Do not release to Kubernetes production if restore paths, secrets, ingress/TLS, probes, or rollback are unknown for the changed services.
