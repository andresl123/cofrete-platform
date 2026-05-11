# Repository Structure

This document aligns the live repository with the Cofrete architecture blueprint. It distinguishes paths that exist now from paths reserved for follow-up Linear issues.

## Current Foundation

```text
cofrete-platform/
  AGENTS.md
  README.md
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

## Planned Infrastructure Structure

COF-003 and COF-004 are expected to add:

```text
.env.example
docker-compose.yml
.github/
  workflows/
    pr-checks.yml
    docker-publish.yml
  pull_request_template.md
```

The concrete CI path is `.github/workflows/pr-checks.yml`; it should run `python scripts/agent_harness_check.py` on pull requests once COF-004 lands.

`docker-compose.yml` is local-only. Kubernetes remains the production runtime once the Kubernetes architecture PR lands.

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
