# Validation

Run from the repository root:

```sh
python scripts/agent_harness_check.py
```

The harness validates durable repository structure and required documentation artifacts. Task handoff must include a task-specific HTML explainer when the task changes implementation, architecture, validation behavior, or service ownership. The explainer should summarize what changed, why it changed, affected files or areas, validation run, and the next task it unblocks when relevant.

## CI Validation Matrix

GitHub Actions runs `.github/workflows/pr-checks.yml` for pull requests, pushes to `main`, and manual dispatches.

| Area | CI behavior | Required command when scaffold exists |
|---|---|---|
| Repository harness | Always run | `python scripts/agent_harness_check.py` |
| Core API | Run when `core-api/pom.xml` exists | `cd core-api && mvn -q validate test` |
| Finance worker | Run when `finance-worker/pom.xml` exists | `cd finance-worker && mvn -q validate test` |
| Data importer worker | Run when `data-importer-worker/pom.xml` exists | `cd data-importer-worker && mvn -q validate test` |
| Mobile app | Run when `mobile-app/package.json` exists | `cd mobile-app && npm ci && npm run typecheck && npm run lint && npm run test:ci` |
| Web app | Run when `web-app/package.json` exists | `cd web-app && npm ci && npm run lint && npm run test && npm run build` |
| Local infrastructure | Run when `docker-compose.yml` exists | `docker compose config` |

CI skips service checks until the corresponding scaffold file exists. The skip is intentional for M0 because this repository currently contains service ownership placeholders before service source code lands.

## Local Service Checks

These commands become required once the corresponding service scaffold exists.

```sh
cd core-api
mvn -q validate test
```

```sh
cd finance-worker
mvn -q validate test
```

```sh
cd data-importer-worker
mvn -q validate test
```

```sh
cd mobile-app
npm ci
npm run typecheck
npm run lint
npm run test:ci
```

```sh
cd web-app
npm ci
npm run lint
npm run test
npm run build
```

```sh
docker compose config
```

## Task Explainer Check

Every task branch with a recognizable issue ID in its branch name, such as `ROU-210` or `COF-006`, must include or update a task-specific HTML explainer under `docs/explainers/`.

The explainer filename should end with `explainer.html`, and the file contents must include every issue ID found in the branch name. This lets `python scripts/agent_harness_check.py` verify that handoff documentation exists without forcing generic branches or `main` to create task-only files.

Explainers are manually maintained derived summaries for understanding what changed. Canonical source-of-truth wording stays in markdown docs such as product, architecture, compliance, runbook, and harness files.

Create or update the explainer before the final validation pass, then run:

```sh
python scripts/agent_harness_check.py
git diff --check
```

## Future Kubernetes Checks

These commands become required once Kubernetes manifests or deployment tooling exist.

```sh
kubectl kustomize deploy/overlays/staging
```

```sh
kubectl apply --dry-run=server -f <rendered-manifests.yaml>
```

```sh
kubectl rollout status deployment/<deployment-name> -n <namespace>
```

Use the Kubernetes REST gateway for read-only cluster inspection when configured. Never print or commit gateway tokens, kubeconfig contents, Secret values, or rendered Secret data.
