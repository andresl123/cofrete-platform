# MVP Launch Runbook

## Release Gate

MVP launch is blocked unless COF-022 smoke validation is green:

```sh
scripts/demo/seed-demo-data.sh
scripts/smoke/local-demo-e2e.sh
scripts/demo/reset-demo-data.sh
```

This smoke flow proves the current synthetic driver/truck/freight path, deterministic finance fixture, pass-through toll behavior, ANP fixture freshness, RNTRC/insurance advisory metadata, expiration-alert scenario, receivables/reserves coverage, and mobile dashboard rendering.

## In MVP

- Driver and truck profile setup for one autonomous driver.
- Freight/trip input, direct costs, diesel, tolls, reserves, expected profit, and safe personal withdrawal.
- Toll and Vale-Pedagio pass-through handling.
- Reserve wallet and receivables/customer payment tracking.
- ANP synthetic diesel fixture and latest-price validation.
- RNTRC, insurance, document, tax, IPVA/licensing, CIOT, freight-floor, and waiting-time advisory metadata.
- Mobile-first financial health dashboard.
- Synthetic demo seed and local smoke validation flow.

## Deferred

- Bank, Pix, payment movement, settlement, and Open Finance integration.
- OCR for receipts/documents.
- gov.br, ANTT, RNTRC Digital, Receita Federal, SUSEP, insurer, DETRAN, SEFAZ, or other official portal login/proxying.
- Automatic CIOT issuance or official update workflows.
- Production paid toll APIs.
- Automated RNTRC public-status lookup.
- Automated ANTT freight-floor table ingestion until source reliability and validation are approved.
- Active company/fleet mode.

## Official-Source Review Checklist

Before launch, re-check:

- ANTT RNTRC and RNTRC Digital guidance.
- ANTT public RNTRC consultation link-only behavior.
- ANTT CIOT and Vale-Pedagio pages.
- ANTT toll plaza open data.
- ANP diesel price source pages and fixture freshness.
- SUSEP/CNSP insurance guidance and insurer/broker caveats.
- Receita Federal and MEI Caminhoneiro guidance.
- State DETRAN/SEFAZ URLs used for IPVA/licensing reminders.

## Risk Register Review

Launch reviewer must confirm each open production risk has one of:

- an MVP control already implemented;
- a documented follow-up issue;
- a release blocker decision.

Current production-sensitive risk areas:

- finance accuracy and exact money treatment;
- toll/Vale-Pedagio pass-through classification;
- compliance wording and official-source links;
- source freshness and stale/unknown state handling;
- privacy/security boundaries for CPF/CNPJ metadata, RNTRC, plate, RENAVAM, insurance, documents, receipts, and receivables;
- unsupported automation for RNTRC, CIOT, paid toll APIs, bank/Pix/Open Finance, and OCR.

## Validation Matrix

Run from the repository root unless a command changes directory:

| Area | Command |
|---|---|
| Harness | `python scripts/agent_harness_check.py` |
| Local infrastructure | `docker compose config` |
| MVP smoke | `scripts/smoke/local-demo-e2e.sh` |
| Core API | `cd core-api && mvn -q validate test` |
| Finance worker | `cd finance-worker && mvn -q validate test` |
| Data importer worker | `cd data-importer-worker && mvn -q validate test` |
| Mobile app | `cd mobile-app && npm run typecheck && npm run lint && npm run test:ci` |
| Web app | Deferred until `web-app/package.json` exists |

## Launch Statement

Cofrete is advisory product software. It is not an official government, legal, tax, accounting, insurance, ANTT, ANP, SUSEP, Receita Federal, DETRAN, SEFAZ, bank, payment, or toll authority channel.
