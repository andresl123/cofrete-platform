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

## Future Kubernetes Checks

These commands become required once Kubernetes manifests or deployment tooling exist.

```sh
kubectl kustomize deploy/overlays/staging
```

```sh
kubectl apply --dry-run=server -f <rendered-manifests.yaml>
```

```sh
kubectl rollout status deployment/<deployment-name> -n <namespace>
```

Use the Kubernetes REST gateway for read-only cluster inspection when configured. Never print or commit gateway tokens, kubeconfig contents, Secret values, or rendered Secret data.
