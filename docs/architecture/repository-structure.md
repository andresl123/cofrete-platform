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

`core-api/` now contains a Java 21 Spring Boot service scaffold with Maven, Dockerfile, source tree, tests, Flyway migrations, and service documentation. `finance-worker/` now contains a Java 21 Spring Boot worker scaffold with Maven, RabbitMQ messaging stubs, tests, and a Dockerfile. `data-importer-worker/` now contains a Java 21 Spring Boot worker scaffold with Maven, Dockerfile, source tree, tests, scheduler/import stubs, synthetic ANP fixture loading, and RabbitMQ publisher stubs. Other unscaffolded service directories currently contain `AGENTS.md` and `README.md` only. Remaining service source code, Dockerfiles, package files, and test folders are introduced by service scaffold issues.

`docker-compose.yml` and `.env.example` define the local-only PostgreSQL, RabbitMQ, and MinIO infrastructure used by upcoming backend and worker scaffolds.

## Documentation Structure

```text
docs/
  product/
  architecture/
  compliance/
  agent-harness/
  runbooks/
  explainers/
```

The docs are source-of-truth files, not generated artifacts. Changes to contracts, service ownership, validation commands, deployment assumptions, or compliance wording must update these docs.

`docs/explainers/` contains manually maintained derived HTML summaries for orientation and task handoff. Explainers are not source-of-truth docs; update canonical markdown first, then realign any relevant explainer.

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
docs/explainers/local-infrastructure-explainer.html
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
  Dockerfile
  src/
    main/
      java/com/cofrete/coreapi/
        profile/
        trip/
        finance/
        reserve/
        compliance/
        receivables/
        imports/
        auth/
        audit/
      resources/
        db/migration/
    test/
      java/com/cofrete/coreapi/

finance-worker/
  pom.xml
  src/
  Dockerfile

data-importer-worker/
  pom.xml
  Dockerfile
  src/
    main/
      java/com/cofrete/dataimporter/
        anp/
        imports/
        messaging/
        scheduling/
      resources/
        fixtures/
    test/
      java/com/cofrete/dataimporter/

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

Service scaffolding issues must update local `README.md` and `AGENTS.md` files with real validation commands. The Core API service validation command is:

```sh
cd core-api && mvn -q validate test
```

The Data Importer Worker service validation command is:

```sh
cd data-importer-worker && mvn -q validate test
```

The Finance Worker service validation command is:

```sh
cd finance-worker && mvn -q validate test
```

The `core-api` package names above are implementation targets, not a requirement to use that exact Java base package path. The important rule is that the Core API starts as an internally modular service with explicit domain boundaries for profile, trip, finance, reserve, compliance, receivables, and imports. Cross-module calls should go through clear application/service interfaces rather than sharing ad hoc persistence or business logic.
