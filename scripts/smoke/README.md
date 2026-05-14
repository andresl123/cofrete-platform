# Smoke Scripts

This directory contains runtime smoke tests defined in `docs/agent-harness/runtime-smoke-tests.md`.

Implemented scripts:

- `local-demo-e2e.sh`
- `validate_mvp_demo_flow.py`

Add executable scripts only when the required services and safe cleanup path exist.

The local demo smoke flow validates a synthetic fixture and runs the narrow service/mobile tests that prove freight finance, pass-through toll behavior, ANP fixture freshness, RNTRC/insurance advisory metadata, expiration alerts, receivables, reserves, and dashboard rendering.
