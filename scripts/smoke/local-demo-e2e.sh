#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "$repo_root"

python scripts/smoke/validate_mvp_demo_flow.py

if [[ -f core-api/pom.xml ]]; then
  (cd core-api && mvn -q -Dtest=ComplianceEndpointTests,TripEndpointTests,ReserveEndpointTests,ReceivableEndpointTests,FuelDataEndpointTests test)
fi

if [[ -f finance-worker/pom.xml ]]; then
  (cd finance-worker && mvn -q -Dtest=TripFinanceCalculatorFixtureTests,ReserveAllocationCalculatorTests test)
fi

if [[ -f data-importer-worker/pom.xml ]]; then
  (cd data-importer-worker && mvn -q -Dtest=AnpDieselPriceFixtureLoaderTests,AnpDieselPriceImportJobTests test)
fi

if [[ -f mobile-app/package.json ]]; then
  (
    cd mobile-app
    if [[ ! -d node_modules ]]; then
      npm ci
    fi
    npm run test:ci -- --runTestsByPath src/__tests__/MobileApp.test.tsx
  )
fi

if [[ -f web-app/package.json ]]; then
  (
    cd web-app
    if [[ ! -d node_modules ]]; then
      npm ci
    fi
    npm run test
  )
else
  printf 'web-app/package.json not present; web validation deferred until web scaffold exists.\n'
fi
