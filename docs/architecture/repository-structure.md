# Repository Structure

This document aligns the live repository with the Cofrete architecture blueprint. It distinguishes paths that exist now from paths reserved for follow-up Linear issues.

## Current Foundation

```text
cofrete-platform/
  AGENTS.md
  README.md
  docker-compose.yml
  .env.example
  .github/
    workflows/
      pr-checks.yml
    pull_request_template.md
  .editorconfig
  .gitattributes
  .gitignore
  core-api/
  finance-worker/
  data-importer-worker/
  mobile-app/
  web-app/
  docs/
  scripts/
```

Each service directory currently contains `AGENTS.md` and `README.md` only. Service source code, Dockerfiles, package files, and test folders are introduced by service scaffold issues.

`docker-compose.yml` and `.env.example` define the local-only PostgreSQL, RabbitMQ, and MinIO infrastructure used by upcoming backend and worker scaffolds.

## Documentation Structure

```text
docs/
  product/
  architecture/
  compliance/
  agent-harness/
  runbooks/
```

The docs are source-of-truth files, not generated artifacts. Changes to contracts, service ownership, validation commands, deployment assumptions, or compliance wording must update these docs.

The canonical RNTRC/ANTT filename is `docs/compliance/rntrc-antt.md`. If a planning note refers to `rntrc-anttt.md`, treat that as a typo and keep the existing corrected filename.

Blueprint-required compliance topics currently have dedicated files for RNTRC/ANTT, CIOT/freight floor, Vale-Pedagio, diesel/ANP, insurance, MEI Caminhoneiro, IPVA/licensing, tax profiles, loading/unloading waiting time, and the official-source register.

## CI Structure

COF-004 adds the first GitHub guardrails:

```text
.github/
  workflows/
    pr-checks.yml
  pull_request_template.md
```

The concrete CI path is `.github/workflows/pr-checks.yml`. It runs `python scripts/agent_harness_check.py` on pull requests and includes scaffold-aware Java, Node, and Docker Compose validation jobs that skip until service scaffolds or `docker-compose.yml` exist.

The pull request template requires scope, validation commands, docs impact, finance/compliance caveats, UI screenshots when relevant, and risk/rollback notes.

## Local Infrastructure Structure

COF-003 adds the local development infrastructure:

```text
.env.example
docker-compose.yml
docs/runbooks/local-infrastructure-explainer.html
.github/
  workflows/
    docker-publish.yml
```

`docker-compose.yml` is local-only and provides PostgreSQL, RabbitMQ, MinIO, a MinIO bucket initializer, named volumes, health checks, and the `cofrete-local` network. Kubernetes remains the intended production runtime.

Later CI/deployment issues are expected to add `.github/workflows/docker-publish.yml`.

## Planned Script Structure

Current:

```text
scripts/
  agent_harness_check.py
  start_task_worktree.sh
```

Reserved for follow-up issues:

```text
scripts/
  smoke/
    local-demo-e2e.sh
    finance-calculation-smoke.sh
    data-importer-smoke.sh
    compliance-center-smoke.sh
  demo/
    seed-demo-data.sh
    reset-demo-data.sh
  ci/
    docker_publish_matrix.py
```

Until real scripts exist, `scripts/smoke/README.md`, `scripts/demo/README.md`, and `scripts/ci/README.md` document the expected ownership and validation contracts.

## Planned Service Structure

```text
core-api/
  pom.xml
  src/
    main/
      .../
        profile/
        trip/
        finance/
        reserve/
        compliance/
        receivables/
        imports/
  Dockerfile

finance-worker/
  pom.xml
  src/
  Dockerfile

data-importer-worker/
  pom.xml
  src/
  Dockerfile

mobile-app/
  package.json
  app.json
  src/
  tests/

web-app/
  package.json
  src/
  tests/
  Dockerfile
```

Service scaffolding issues must update local `README.md` and `AGENTS.md` files with real validation commands.

The `core-api` package names above are implementation targets, not a requirement to use that exact Java base package path. The important rule is that the Core API starts as an internally modular service with explicit domain boundaries for profile, trip, finance, reserve, compliance, receivables, and imports. Cross-module calls should go through clear application/service interfaces rather than sharing ad hoc persistence or business logic.
