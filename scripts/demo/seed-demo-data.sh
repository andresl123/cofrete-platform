#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
seed_file="$repo_root/scripts/demo/cofrete-mvp-demo-seed.json"
target_file="${1:-$repo_root/.tmp/cofrete-mvp-demo-seed.json}"

mkdir -p "$(dirname "$target_file")"
cp "$seed_file" "$target_file"

printf 'Synthetic Cofrete MVP demo seed written to %s\n' "$target_file"
printf 'Seed is local-only and contains no real CPF, CNPJ, RNTRC, plate, policy, customer, or payment data.\n'
