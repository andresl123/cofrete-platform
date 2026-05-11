# Local Development Runbook

## Current State

Only repository foundation and docs exist. Service-specific commands become active as scaffolding issues land.

## Baseline Check

```sh
python scripts/agent_harness_check.py
```

## Expected Future Flow

1. Start local infrastructure with Docker Compose.
2. Run Core API against local PostgreSQL.
3. Run workers against local RabbitMQ.
4. Run mobile/web clients against local Core API.
5. Use demo seed for end-to-end validation.

## Troubleshooting

- If harness fails, add the missing doc, service ownership file, or required text marker.
- If local infrastructure is missing, complete COF-003 before running service scaffolds.
