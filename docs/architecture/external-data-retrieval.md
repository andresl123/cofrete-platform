# External Data Retrieval

This document defines how Cofrete should retrieve, normalize, audit, and expose external-source data used in driver-facing decisions.

Cofrete is advisory product software. External data can improve precision, but it must not make the app appear to be an official government, legal, tax, accounting, insurance, or toll certification channel.

## Retrieval Principles

- Mobile and web clients must not fetch government spreadsheets, official portals, or third-party datasets directly.
- `data-importer-worker` owns scheduled/batch imports, parser validation, retries, freshness status, and import audit records.
- `core-api` owns normalized persistence, synchronous APIs, source metadata, and driver-specific overrides.
- `finance-worker` owns deterministic calculations that use normalized external data and driver-entered data.
- `mobile-app` displays source, period, confidence, freshness, and manual correction options.
- Previous known-good data can be used only when freshness is visible and confidence is lowered.
- If a source is unavailable, ambiguous, legally risky, or stale, the driver-facing result must degrade to advisory or unknown instead of pretending precision.

## Retrieval Flow

```text
Official/source data
        |
        v
data-importer-worker
  - fetch/download source data
  - validate parser/schema
  - normalize records
  - create ImportJob audit record
  - publish import completion event when applicable
        |
        v
PostgreSQL normalized tables
        |
        v
core-api modules
  - imports
  - trip
  - compliance
  - profile
  - receivables
        |
        v
finance-worker
  - profitability
  - reserve allocation
  - fuel estimate
  - waiting-time impact
        |
        v
mobile-app / web-app
  - source
  - confidence
  - freshness
  - advisory warnings
```

## Import Audit Contract

Every external import job should record:

```text
source
dataset
status
started_at
completed_at
source_url
source_period_start
source_period_end
retrieved_at
row_count
file_hash
freshness_status
error_summary
correlation_id
```

Normalized external records should carry:

```text
source
source_url
source_period
retrieved_at
import_job_id
confidence
created_at
updated_at
```

Use these freshness statuses unless an implementation issue explicitly narrows the enum:

| Status | Meaning | Driver-facing behavior |
|---|---|---|
| `CURRENT` | Source data is within the accepted age for the dataset. | Show normal estimate with source and period. |
| `STALE` | Previous known-good data exists but is older than expected. | Show estimate with stale warning and lower confidence. |
| `FAILED` | Latest import failed and no safe current data exists. | Use manual input or unknown state where possible. |
| `UNKNOWN` | Freshness cannot be established. | Show conservative warning and avoid precise claims. |

## Source Matrix And Linear Ownership

| Data area | Retrieval strategy | Primary owner | Linear issue |
|---|---|---|---|
| Importer framework | Scaffold Java worker, scheduler/messaging structure, import job stubs, event publishers, source-data rules. | `data-importer-worker` | [ROU-211 / COF-007](https://linear.app/routing-worker/issue/ROU-211/cof-007-scaffold-data-importer-worker-service) |
| ANP diesel prices | Import ANP diesel fixture/source data, expose latest/history, include source type, period, freshness, confidence. | `data-importer-worker`, `core-api.imports`, `finance-worker` | [ROU-218 / COF-014](https://linear.app/routing-worker/issue/ROU-218/cof-014-implement-anp-diesel-importer-with-fixtures) |
| ANTT toll plazas and tariffs | Import ANTT toll plaza/tariff open data, normalize plaza/tariff records, publish `toll-data.import.completed`. | `data-importer-worker`, `core-api.imports` | [ROU-246 / COF-025](https://linear.app/routing-worker/issue/ROU-246/cof-025-implement-antt-toll-plaza-and-tariff-importer) |
| Toll and Vale-Pedagio classification | Classify pass-through, driver-paid, included-in-freight, no-toll, and unknown states. | `core-api.trip`, `finance-worker` | [ROU-219 / COF-015](https://linear.app/routing-worker/issue/ROU-219/cof-015-implement-toll-and-vale-pedagio-classification-module) |
| ANTT freight floor | Design source-backed import or manual/admin fallback. Do not automate production enforcement until source reliability is proven. | `core-api.compliance`, `data-importer-worker` after design | [ROU-247 / COF-026](https://linear.app/routing-worker/issue/ROU-247/cof-026-design-source-backed-antt-freight-floor-import-workflow) |
| IPVA and licensing rules | Maintain state/year/vehicle-type rules with source URL, reviewed timestamp, and stale/unknown behavior. | `core-api.profile`, `core-api.compliance` | [ROU-248 / COF-027](https://linear.app/routing-worker/issue/ROU-248/cof-027-add-source-backed-ipva-and-licensing-rule-workflow) |
| MEI and Pessoa Fisica tax rules | Maintain source-backed `TaxRuleYear` and PF planning rules with effective dates, source URL, and conservative warnings. | `core-api.profile`, `finance-worker` where calculations use tax reserves | [ROU-249 / COF-028](https://linear.app/routing-worker/issue/ROU-249/cof-028-add-tax-rule-source-update-workflow-for-mei-and-pessoa-fisica) |
| RNTRC public status | Manual/link-only MVP workflow. Cofrete stores driver-entered advisory metadata and returns unsupported/manual-link guidance for public lookup requests until ANTT source/API terms are approved. Keep gov.br credentials outside Cofrete. | `core-api.compliance` | [ROU-250 / COF-029](https://linear.app/routing-worker/issue/ROU-250/cof-029-evaluate-rntrc-public-status-lookup-automation) |
| Insurance regulatory guidance | Maintain source-review metadata for SUSEP/official/professional guidance and distinguish those rules from driver-entered policy records. | `core-api.compliance` | [ROU-251 / COF-030](https://linear.app/routing-worker/issue/ROU-251/cof-030-add-insurance-regulatory-source-refresh-workflow) |
| Waiting-time rules | Maintain threshold/rate/effective-date configuration with source URL, reviewed timestamp, and advisory wording. | `core-api.compliance`, `finance-worker` | [ROU-252 / COF-031](https://linear.app/routing-worker/issue/ROU-252/cof-031-add-waiting-time-rule-source-backed-configuration-workflow) |
| Compliance center wording and metadata | Expose RNTRC, insurance, documents, alerts, compliance calendar, and official-channel caveats to mobile. | `core-api.compliance`, `mobile-app` | [ROU-220 / COF-016](https://linear.app/routing-worker/issue/ROU-220/cof-016-implement-compliance-center-data-model-and-wording) |
| End-to-end proof | Smoke test ANP import, toll pass-through behavior, RNTRC/insurance metadata, expiration alerts, and dashboard rendering. | `scripts`, services, mobile/web | [ROU-226 / COF-022](https://linear.app/routing-worker/issue/ROU-226/cof-022-add-smoke-tests-demo-seed-and-mvp-validation-flow) |

## Source Handling By Risk

### Automated Import Candidates

These can be implemented as backend importer jobs when source format, licensing, and fixture tests are understood:

- ANP diesel price data.
- ANTT toll plaza and tariff data.
- ANTT freight-floor tables only after COF-026 confirms source reliability and validation rules.

### Source-Backed Admin Rules

These should start as admin/manual configuration with source URL, effective dates, and reviewed timestamp:

- MEI Caminhoneiro and Pessoa Fisica tax-planning rules.
- IPVA/licensing state rules.
- Waiting-time thresholds and rates.
- Insurance regulatory wording and requirement assumptions.

### Link-Or-Evaluate First

These should not be automated until terms, privacy, security, and compliance wording are reviewed:

- RNTRC public-status lookup.
- Any gov.br, ANTT, Receita Federal, insurer, broker, DETRAN, or SEFAZ flow that would require user credentials.
- Paid toll provider integration or production-grade paid route/toll APIs.

## Driver-Facing Requirements

External data that affects a driver decision must expose:

```text
value
source
source_type
source_period
retrieved_at or reviewed_at
confidence
freshness_status
calculation_trace_id when used in finance output
import_audit_id when imported
```

When freshness is `STALE`, `FAILED`, or `UNKNOWN`, the app should:

- show conservative wording;
- allow manual driver input where appropriate;
- avoid exact legal/compliance claims;
- keep previous known-good values visibly marked if used;
- trigger recalculation when newer data arrives.

## Related Docs

- `docs/compliance/official-source-register.md`
- `docs/architecture/service-boundaries.md`
- `docs/architecture/api-contracts.md`
- `docs/architecture/event-contracts.md`
- `docs/architecture/data-model.md`
- `docs/runbooks/data-import-failure.md`
