#!/usr/bin/env python3
"""Validate the repository foundation expected by agent tasks."""

from __future__ import annotations

import os
from pathlib import Path
import re
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]
TASK_ID_PATTERN = re.compile(r"\b(?:ROU|COF)-\d+\b", re.IGNORECASE)

SERVICE_DIRS = (
    "core-api",
    "finance-worker",
    "data-importer-worker",
    "mobile-app",
    "web-app",
)

DOC_DIRS = (
    "docs/product",
    "docs/architecture",
    "docs/compliance",
    "docs/agent-harness",
    "docs/runbooks",
    "docs/explainers",
)

REQUIRED_DIRS = (
    ".github",
    ".github/workflows",
    "docs",
    "scripts",
    *SERVICE_DIRS,
    *DOC_DIRS,
)

REQUIRED_FILES = (
    "AGENTS.md",
    "CONTEXT.md",
    "README.md",
    ".env.example",
    ".editorconfig",
    ".gitattributes",
    ".gitignore",
    "docker-compose.yml",
    ".github/pull_request_template.md",
    ".github/workflows/pr-checks.yml",
    "docs/README.md",
    "docs/explainers/application-explainer.html",
    "docs/product/brazil-trucker-finance-blueprint.md",
    "docs/product/mvp-scope.md",
    "docs/product/product-principles.md",
    "docs/product/user-personas.md",
    "docs/product/glossary-pt-br.md",
    "docs/product/roadmap.md",
    "docs/architecture/system-overview.md",
    "docs/explainers/architecture-explainer.html",
    "docs/architecture/repository-structure.md",
    "docs/architecture/service-boundaries.md",
    "docs/architecture/data-model.md",
    "docs/architecture/api-contracts.md",
    "docs/architecture/event-contracts.md",
    "docs/architecture/kubernetes.md",
    "docs/architecture/security.md",
    "docs/architecture/auth.md",
    "docs/architecture/observability.md",
    "docs/architecture/deployment.md",
    "docs/explainers/core-api-scaffold-explainer.html",
    "docs/explainers/data-importer-worker-scaffold-explainer.html",
    "docs/compliance/rntrc-antt.md",
    "docs/compliance/vale-pedagio.md",
    "docs/compliance/diesel-anp.md",
    "docs/compliance/insurance.md",
    "docs/compliance/mei-caminhoneiro.md",
    "docs/compliance/ciot-freight-floor.md",
    "docs/compliance/ipva-licensing.md",
    "docs/compliance/tax-profiles.md",
    "docs/compliance/waiting-time.md",
    "docs/compliance/official-source-register.md",
    "docs/agent-harness/README.md",
    "docs/agent-harness/workflow.md",
    "docs/agent-harness/architecture.md",
    "docs/agent-harness/validation.md",
    "docs/agent-harness/runtime-smoke-tests.md",
    "docs/agent-harness/worktree-development.md",
    "docs/agent-harness/frontend-review-loop.md",
    "docs/agent-harness/testing-policy.md",
    "docs/agent-harness/golden-principles.md",
    "docs/agent-harness/observability.md",
    "docs/agent-harness/risk-register.md",
    "docs/explainers/ci-pr-harness-explainer.html",
    "docs/runbooks/local-development.md",
    "docs/explainers/local-infrastructure-explainer.html",
    "docs/runbooks/demo-seed.md",
    "docs/runbooks/data-import-failure.md",
    "docs/runbooks/incident-response.md",
    "docs/runbooks/production-release.md",
    "scripts/agent_harness_check.py",
    "scripts/start_task_worktree.sh",
    "scripts/smoke/README.md",
    "scripts/demo/README.md",
    "scripts/ci/README.md",
    *(f"{service}/AGENTS.md" for service in SERVICE_DIRS),
    *(f"{service}/README.md" for service in SERVICE_DIRS),
)

REQUIRED_TEXT = {
    "AGENTS.md": (
        "CONTEXT.md",
        "docs/agent-harness/README.md",
        "python scripts/agent_harness_check.py",
        "task-specific HTML explainer",
        "Do not commit, push, open a PR, or update an existing PR",
    ),
    ".github/pull_request_template.md": (
        "Validation",
        "Docs Impact",
        "Finance / Compliance Caveats",
        "Screenshots or recordings attached for UI changes",
        "python scripts/agent_harness_check.py",
    ),
    ".github/workflows/pr-checks.yml": (
        "python scripts/agent_harness_check.py",
        "mvn -q validate test",
        "npm run test:ci",
        "npm run build",
        "docker compose config",
    ),
    "docs/agent-harness/workflow.md": (
        "task-specific HTML explainer",
        "frontend-review-loop.md",
        "Do not commit, push, open a PR, or update an existing PR",
    ),
    "docs/agent-harness/validation.md": (
        "CI Validation Matrix",
        "python scripts/agent_harness_check.py",
        "mvn -q validate test",
        "npm run test:ci",
        "docker compose config",
        "task-specific HTML explainer",
        "frontend-review-loop.md",
    ),
    "docs/agent-harness/frontend-review-loop.md": (
        "Run this loop only when the task changes user-facing frontend or mobile files",
        "Run up to 15 Impeccable passes",
        "Do not run the loop for backend-only",
        "Required Handoff Summary",
    ),
    "scripts/ci/README.md": (
        ".github/workflows/pr-checks.yml",
        "pull_request",
        "python scripts/agent_harness_check.py",
    ),
    "README.md": (
        "not an official government, legal, tax, accounting, or insurance channel",
        "docs/architecture/api-contracts.md",
        ".github/workflows/pr-checks.yml",
        "docker compose config",
    ),
    ".env.example": (
        "COFRETE_CORE_API_DATABASE_URL",
        "COFRETE_FINANCE_WORKER_AMQP_URL",
        "COFRETE_DATA_IMPORTER_OBJECT_STORAGE_BUCKET",
    ),
    "docker-compose.yml": (
        "postgres:16-alpine",
        "rabbitmq:3.13-management-alpine",
        "minio/minio",
        "cofrete-local",
    ),
    "CONTEXT.md": (
        "Canonical Markdown Doc",
        "Explainer Page",
        "Derived Entry Point",
    ),
    "docs/README.md": (
        "derived entry points",
        "explainers/application-explainer.html",
        "explainers/architecture-explainer.html",
    ),
    "docs/compliance/official-source-register.md": (
        "ANTT",
        "ANP",
        "SUSEP",
        "Receita Federal",
    ),
    "docs/architecture/api-contracts.md": (
        "GET /api/drivers/me",
        "POST /api/tax-profile",
        "POST /api/trips/{tripId}/profitability-estimate",
        "POST /api/trips/{tripId}/acceptance-decision",
        "POST /api/trips/{tripId}/vale-pedagio",
        "GET /api/compliance/score",
        "GET /api/documents",
    ),
    "docs/architecture/event-contracts.md": (
        "trip.recalculation.requested",
        "trip.finance.recalculated",
        "reserve.allocation.requested",
        "fuel-price.import.completed",
        "\"version\": 1",
    ),
    "docs/architecture/data-model.md": (
        "tax_rule_year",
        "ipva_rule",
        "TripProfitabilitySnapshot",
        "WaitingTimeRecord",
    ),
    "docs/compliance/ciot-freight-floor.md": (
        "CIOT required?",
        "Freight-Floor Checker",
    ),
    "docs/compliance/waiting-time.md": (
        "extra_waiting_charge",
        "waiting_time_rule",
    ),
    "docs/architecture/repository-structure.md": (
        "scripts/smoke",
        ".github/workflows/pr-checks.yml",
        "docker-compose.yml",
    ),
    "docs/runbooks/local-development.md": (
        "docker compose config",
        "cofrete-postgres",
        "cofrete-rabbitmq",
        "cofrete-minio",
        "COFRETE_CORE_API_DATABASE_URL",
    ),
    "core-api/README.md": (
        "COFRETE_CORE_API_DATABASE_URL",
        "jdbc:postgresql://postgres:5432/cofrete_local",
    ),
    "finance-worker/README.md": (
        "COFRETE_FINANCE_WORKER_AMQP_URL",
        "amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete",
    ),
    "data-importer-worker/README.md": (
        "COFRETE_DATA_IMPORTER_OBJECT_STORAGE_BUCKET",
        "http://minio:9000",
    ),
    "docs/agent-harness/runtime-smoke-tests.md": (
        "finance-calculation-smoke.sh",
        "toll reimbursement does not increase profit",
    ),
    "docs/agent-harness/worktree-development.md": (
        "scripts/start_task_worktree.sh",
        "../cofrete-platform-worktrees/",
    ),
    "docs/architecture/deployment.md": (
        "Kubernetes is the intended production runtime",
        "docs/architecture/kubernetes.md",
    ),
    "docs/architecture/kubernetes.md": (
        "Current Cluster Access Status",
        "Kubernetes Secrets",
        "IngressClass",
    ),
    "docs/explainers/application-explainer.html": (
        "manually maintained derived summary",
    ),
    "docs/explainers/architecture-explainer.html": (
        "manually maintained derived summary",
    ),
    "docs/explainers/core-api-scaffold-explainer.html": (
        "ROU-209 / COF-005",
        "Core API service scaffold",
        "cd core-api && mvn -q validate test",
        "docker compose config",
        "python scripts/agent_harness_check.py",
        "Live PostgreSQL startup smoke",
    ),
    "docs/explainers/data-importer-worker-scaffold-explainer.html": (
        "ROU-211 / COF-007",
        "Data Importer Worker service scaffold",
        "fuel-price.import.completed",
        "toll-data.import.completed",
        "cd data-importer-worker && mvn -q validate test",
        "docker compose config",
        "python scripts/agent_harness_check.py",
        "No production paid toll provider integration",
    ),
    "docs/compliance/diesel-anp.md": (
        "driver_confirmed",
        "official_weekly",
        "truck_consumption_profile",
    ),
    "docs/compliance/ipva-licensing.md": (
        "ipva_rule",
        "never use one national hardcoded truck percentage",
    ),
    "docs/compliance/mei-caminhoneiro.md": (
        "tax_rule_year",
        "Do not hardcode the annual limit",
    ),
}


def _missing_paths(paths: tuple[str, ...], expected_type: str) -> list[str]:
    missing: list[str] = []

    for relative_path in paths:
        path = ROOT / relative_path
        exists = path.is_dir() if expected_type == "directory" else path.is_file()
        if not exists:
            missing.append(relative_path)

    return missing


def _content_failures(required_text: dict[str, tuple[str, ...]]) -> list[str]:
    failures: list[str] = []

    for relative_path, expected_values in required_text.items():
        path = ROOT / relative_path
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for expected_value in expected_values:
            if expected_value not in text:
                failures.append(f"{relative_path} missing text: {expected_value}")

    return failures


def _current_branch_name() -> str:
    github_branch = os.environ.get("GITHUB_HEAD_REF") or os.environ.get("GITHUB_REF_NAME")
    if github_branch:
        return github_branch

    try:
        result = subprocess.run(
            ["git", "branch", "--show-current"],
            cwd=ROOT,
            check=False,
            capture_output=True,
            text=True,
        )
    except OSError:
        return ""

    return result.stdout.strip()


def _task_ids_from_branch(branch_name: str) -> tuple[str, ...]:
    task_ids = {match.group(0).upper() for match in TASK_ID_PATTERN.finditer(branch_name)}
    return tuple(sorted(task_ids))


def _task_explainer_failures(task_ids: tuple[str, ...]) -> list[str]:
    if not task_ids:
        return []

    candidates = sorted((ROOT / "docs" / "explainers").glob("*explainer.html"))
    matching_explainers: list[str] = []

    for path in candidates:
        text = path.read_text(encoding="utf-8")
        if all(task_id in text.upper() for task_id in task_ids):
            matching_explainers.append(str(path.relative_to(ROOT)))

    if matching_explainers:
        return []

    return [
        "task branch contains issue ID(s) "
        + ", ".join(task_ids)
        + " but no docs/explainers/*explainer.html file includes all of them"
    ]


def main() -> int:
    missing_dirs = _missing_paths(REQUIRED_DIRS, "directory")
    missing_files = _missing_paths(REQUIRED_FILES, "file")
    content_failures = _content_failures(REQUIRED_TEXT)
    branch_name = _current_branch_name()
    task_ids = _task_ids_from_branch(branch_name)
    task_explainer_failures = _task_explainer_failures(task_ids)

    if missing_dirs or missing_files or content_failures or task_explainer_failures:
        print("agent harness check failed", file=sys.stderr)
        for path in missing_dirs:
            print(f"missing directory: {path}", file=sys.stderr)
        for path in missing_files:
            print(f"missing file: {path}", file=sys.stderr)
        for failure in content_failures:
            print(f"content failure: {failure}", file=sys.stderr)
        for failure in task_explainer_failures:
            print(f"task explainer failure: {failure}", file=sys.stderr)
        return 1

    print("agent harness check passed")
    print(f"checked {len(REQUIRED_DIRS)} directories and {len(REQUIRED_FILES)} files")
    print(f"checked {sum(len(values) for values in REQUIRED_TEXT.values())} required text markers")
    if task_ids:
        print(f"checked task explainer for {', '.join(task_ids)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
