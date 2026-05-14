# Demo Scripts

This directory is reserved for repeatable demo data setup and reset scripts.

Implemented scripts:

- `seed-demo-data.sh`
- `reset-demo-data.sh`
- `cofrete-mvp-demo-seed.json`

Demo data must be synthetic and must not contain real CPF, CNPJ, RNTRC, policy, plate, customer, or payment data.

Run `scripts/smoke/local-demo-e2e.sh` from the repository root after seeding to validate the fixture, pass-through finance treatment, ANP sample price, compliance alert scenario, and mobile dashboard smoke coverage.
