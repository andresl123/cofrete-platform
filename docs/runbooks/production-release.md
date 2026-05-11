# Production Release Runbook

## Pre-Release Checklist

- Repository harness passes.
- Changed services pass their validation commands.
- Demo seed and smoke flow pass.
- Compliance source register reviewed.
- Risk register reviewed.
- Finance calculations have deterministic tests.
- User-facing compliance wording remains advisory.
- No real secrets or production credentials are committed.

## Compliance Review

Review docs and UI copy touching:

- RNTRC/ANTT.
- CIOT.
- Vale-Pedagio and tolls.
- ANP diesel data.
- SUSEP/insurance.
- MEI Caminhoneiro.
- IPVA/licensing.
- Tax profiles.

## Release Decision

Do not release if the app presents itself as an official government, legal, tax, accounting, or insurance channel.
