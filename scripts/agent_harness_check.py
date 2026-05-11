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

REQUIRED_DIRS = (
    "docs",
    "scripts",
    *SERVICE_DIRS,
)

REQUIRED_FILES = (
    "AGENTS.md",
    "README.md",
    ".editorconfig",
    ".gitattributes",
    ".gitignore",
    "docs/README.md",
    "scripts/agent_harness_check.py",
    *(f"{service}/AGENTS.md" for service in SERVICE_DIRS),
    *(f"{service}/README.md" for service in SERVICE_DIRS),
)


def _missing_paths(paths: tuple[str, ...], expected_type: str) -> list[str]:
    missing: list[str] = []

    for relative_path in paths:
        path = ROOT / relative_path
        exists = path.is_dir() if expected_type == "directory" else path.is_file()
        if not exists:
            missing.append(relative_path)

    return missing


def main() -> int:
    missing_dirs = _missing_paths(REQUIRED_DIRS, "directory")
    missing_files = _missing_paths(REQUIRED_FILES, "file")

    if missing_dirs or missing_files:
        print("agent harness check failed", file=sys.stderr)
        for path in missing_dirs:
            print(f"missing directory: {path}", file=sys.stderr)
        for path in missing_files:
            print(f"missing file: {path}", file=sys.stderr)
        return 1

    print("agent harness check passed")
    print(f"checked {len(REQUIRED_DIRS)} directories and {len(REQUIRED_FILES)} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
