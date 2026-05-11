#!/usr/bin/env python3
"""Validate the repository foundation expected by agent tasks."""

from __future__ import annotations

from pathlib import Path
import sys


ROOT = Path(__file__).resolve().parents[1]

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
)

REQUIRED_DIRS = (
    "docs",
    "scripts",
    *SERVICE_DIRS,
    *DOC_DIRS,
)

REQUIRED_FILES = (
    "AGENTS.md",
    "README.md",
    ".editorconfig",
    ".gitattributes",
    ".gitignore",
    "docs/README.md",
    "docs/product/brazil-trucker-finance-blueprint.md",
    "docs/product/mvp-scope.md",
    "docs/product/product-principles.md",
    "docs/product/user-personas.md",
    "docs/product/glossary-pt-br.md",
    "docs/product/roadmap.md",
    "docs/architecture/system-overview.md",
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
    "docs/agent-harness/testing-policy.md",
    "docs/agent-harness/golden-principles.md",
    "docs/agent-harness/observability.md",
    "docs/agent-harness/risk-register.md",
    "docs/runbooks/local-development.md",
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
        "docs/agent-harness/README.md",
        "python scripts/agent_harness_check.py",
    ),
    "README.md": (
        "not an official government, legal, tax, accounting, or insurance channel",
        "docs/architecture/api-contracts.md",
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


def main() -> int:
    missing_dirs = _missing_paths(REQUIRED_DIRS, "directory")
    missing_files = _missing_paths(REQUIRED_FILES, "file")
    content_failures = _content_failures(REQUIRED_TEXT)

    if missing_dirs or missing_files or content_failures:
        print("agent harness check failed", file=sys.stderr)
        for path in missing_dirs:
            print(f"missing directory: {path}", file=sys.stderr)
        for path in missing_files:
            print(f"missing file: {path}", file=sys.stderr)
        for failure in content_failures:
            print(f"content failure: {failure}", file=sys.stderr)
        return 1

    print("agent harness check passed")
    print(f"checked {len(REQUIRED_DIRS)} directories and {len(REQUIRED_FILES)} files")
    print(f"checked {sum(len(values) for values in REQUIRED_TEXT.values())} required text markers")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
