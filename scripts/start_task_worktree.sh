#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: scripts/start_task_worktree.sh <task-name> [base-branch]" >&2
  exit 2
fi

task_name="$1"
base_branch="${2:-origin/main}"

repo_root="$(git rev-parse --show-toplevel)"
repo_name="$(basename "$repo_root")"
worktrees_root="$(dirname "$repo_root")/${repo_name}-worktrees"

safe_task_name="$(printf '%s' "$task_name" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9._-]+/-/g; s/^-+//; s/-+$//')"

if [[ -z "$safe_task_name" ]]; then
  echo "task name must contain at least one letter or number" >&2
  exit 2
fi

branch_name="codex/${safe_task_name}"
worktree_path="${worktrees_root}/${safe_task_name}"

mkdir -p "$worktrees_root"

git fetch origin
git worktree add "$worktree_path" -b "$branch_name" "$base_branch"

echo "$worktree_path"
