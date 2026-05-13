# CI Scripts

GitHub Actions currently lives in `.github/workflows/pr-checks.yml`.

The `pr-checks.yml` workflow runs on `pull_request`, pushes to `main`, and manual dispatches. It always runs:

```sh
python scripts/agent_harness_check.py
```

The same workflow also contains scaffold-aware jobs for:

- Java services with `mvn -q validate test`.
- Mobile app checks with `npm ci`, `npm run typecheck`, `npm run lint`, and `npm run test:ci`.
- Web app checks with `npm ci`, `npm run lint`, `npm run test`, and `npm run build`.
- Docker Compose validation with `docker compose config`.

Those checks are skipped until their scaffold files exist.

Planned script:

- `docker_publish_matrix.py`

CI helpers must not print secrets or assume production deployment from pull requests.
