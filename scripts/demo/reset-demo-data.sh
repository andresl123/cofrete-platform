#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
target_file="${1:-$repo_root/.tmp/cofrete-mvp-demo-seed.json}"

if [[ -f "$target_file" ]]; then
  rm "$target_file"
  printf 'Removed local synthetic demo seed at %s\n' "$target_file"
else
  printf 'No local synthetic demo seed found at %s\n' "$target_file"
fi
