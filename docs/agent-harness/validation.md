# Validation

Run from the repository root:

```sh
python scripts/agent_harness_check.py
```

## Future Service Checks

These commands become required once the corresponding service scaffold exists.

```sh
cd core-api
mvn -q validate test
```

```sh
cd finance-worker
mvn -q validate test
```

```sh
cd data-importer-worker
mvn -q validate test
```

```sh
cd mobile-app
npm ci
npm run typecheck
npm run lint
npm run test:ci
```

```sh
cd web-app
npm ci
npm run lint
npm run test
npm run build
```

```sh
docker compose config
```
