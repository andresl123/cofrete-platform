# Validation

Run from the repository root:

```sh
python scripts/agent_harness_check.py
```

The harness validates durable repository structure and required documentation artifacts. Task handoff must include a task-specific HTML explainer when the task changes implementation, architecture, validation behavior, or service ownership. The explainer should summarize what changed, why it changed, affected files or areas, validation run, and the next task it unblocks when relevant.

For implemented Linear issues, task handoff must also include the `grill-with-docs` review-and-fix loop summary described in `linear-review-loop.md`. Run the loop before the final validation pass so any fixes it discovers are covered by the final service checks and harness check.

For tasks that change frontend or mobile files, task handoff must also include the Impeccable review loop summary described in `frontend-review-loop.md`. Run that loop after implementation and after the relevant frontend/mobile checks pass at least once, but before `grill-with-docs` and before the final validation pass.

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

## Linear Review Loop Check

For every implemented Linear issue, run up to 10 `grill-with-docs` review loops before handoff.

The handoff should state:

- number of loops executed;
- issues found;
- fixes applied;
- tests and checks run after the loop;
- remaining risks or assumptions.

Stop early when no meaningful issue remains. Do not continue looping only to reach 10 iterations.

## Frontend Review Loop Check

For every task that changes frontend or mobile files, run up to 15 Impeccable review passes before handoff.

The handoff should state:

- why the loop was triggered;
- number of Impeccable passes executed;
- commands run in order;
- issues found;
- fixes applied;
- frontend/mobile tests and checks run after the loop;
- remaining UI risks or assumptions.

Stop early when no meaningful frontend/mobile issue remains. Do not run this loop for backend-only, worker-only, infrastructure-only, or documentation-only tasks unless they also change frontend or mobile files.

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
