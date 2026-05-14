# CIOT And Minimum Freight Floor

Cofrete should track CIOT and freight-floor context as advisory pre-trip compliance support. It does not certify that a freight is legal or compliant.

## Pre-Trip Checklist

```text
CIOT required?
CIOT provided?
Freight above ANTT minimum estimate?
MDF-e/CIOT linked when applicable?
Payment method registered?
Vale-Pedagio confirmed?
Mandatory insurance proof confirmed?
```

## Risk Messages

| Missing or risky item | App warning |
|---|---|
| CIOT missing | "CIOT was not confirmed. Check the contract before starting the trip." |
| Freight below floor | "This freight may be below the ANTT minimum estimate." |
| Payment not registered | "Payment method was not confirmed. Cash-flow risk is high." |
| Missing Vale-Pedagio | "Vale-Pedagio was not confirmed. Tolls may reduce your cash flow." |

## Freight-Floor Checker

The MVP can start with a manual or placeholder comparison workflow while the official ANTT freight-floor table importer is designed.

Inputs:

| Input | Example |
|---|---|
| Origin and destination | Goiania to Santos |
| Distance | Loaded km plus empty return km |
| Vehicle | Number of axles |
| Cargo type | General, refrigerated, bulk, dangerous cargo |
| Operation type | Lotacao, fracionada, TAC-agregado |
| Offered freight | BRL amount |

Output:

```text
above_floor
below_floor
unknown
margin_amount
source
source_period
confidence
```

This is a high-risk compliance feature. Production automation needs source-backed tests, official-source review, and careful wording.

## Source-Backed Import Workflow Design

Source review date: 2026-05-14.

Primary official sources:

- ANTT freight-floor hub: https://www.gov.br/antt/pt-br/assuntos/cargas/pagamento-eletronico-de-fretes-pef-ciot/piso-minimo-do-frete
- ANTT March 2026 update note: https://www.gov.br/antt/pt-br/assuntos/ultimas-noticias/antt-atualiza-tabela-dos-pisos-minimos-de-frete-em-decorrencia-da-variacao-no-preco-do-diesel-s10
- DOU publication linked from ANTT updates, such as Portaria SUROC nº 3/2026.
- ANTT calculator referenced by ANTT: https://calculadorafrete.antt.gov.br

Current official-source observations:

- ANTT states that the freight-floor table is updated every six months or when Diesel S10 varies by more than 5%.
- ANTT publishes updated values by operation type and axle count in portarias, with DOU links used as authoritative legal publication.
- The published table model uses operation/table groups such as Tabela A, B, C, and D, cargo specificity, distance, axle assumptions, and coefficients.
- ANTT's 2026 CIOT changes make freight-floor validation higher risk because official systems may block under-floor operations. Cofrete must not present its own checker as official validation.

### Import Stages

1. Discover source candidates from the ANTT freight-floor hub and official-source register.
2. Resolve the latest DOU/ANTT publication and capture source URL, publication identifier, publication date, retrieval time, file hash or HTML hash, and parser version.
3. Classify source format:
   - structured HTML table;
   - PDF with extractable tables;
   - DOU HTML/text;
   - calculator-only source with no stable bulk table.
4. Parse into a staging table with raw rows, row coordinates, detected table label, cargo type, operation type, axle count, coefficient names, coefficient values, effective start/end, and confidence.
5. Run semantic validation:
   - expected table labels present;
   - expected axle ranges present;
   - monetary/decimal coefficients parse exactly;
   - source period does not overlap an already approved period unless it supersedes it;
   - sample synthetic scenarios produce deterministic `above_floor`, `below_floor`, and `unknown` outcomes.
6. Require manual approval before promoting a source period from staging to active if source format, table count, cargo labels, coefficient names, or effective-period semantics changed.
7. Promote approved rows to read models used by the checker.
8. Keep failed or unapproved imports visible as `unknown` checker confidence, not as stale legal precision.

### Parser Strategy

The first implementation should prefer a source adapter chain:

| Adapter | Use when | Output confidence |
|---|---|---|
| `dou_html_table` | DOU publication exposes stable HTML/text tables. | `official_dou_parsed` |
| `antt_html_table` | ANTT page embeds direct coefficient tables. | `official_antt_parsed` |
| `pdf_table_extract` | Official source is a PDF with extractable tables. | `official_pdf_needs_review` until manually approved |
| `manual_admin_entry` | Format changed, calculator-only source, or parser cannot prove semantics. | `manual_official_source_reviewed` |

Parser output must preserve raw source references. It must not normalize labels destructively; store canonical fields and original text side by side.

### Proposed Data Model

Source period:

| Field | Purpose |
|---|---|
| `id` | Application ID, such as `freight_floor_period_123`. |
| `source` | `ANTT`, `DOU`, or both when linked. |
| `source_url` | Official page or DOU publication URL. |
| `source_publication_id` | Portaria/resolution identifier. |
| `source_period_start` | Date the coefficients become effective. |
| `source_period_end` | Nullable end date. |
| `diesel_reference_price` | Reference Diesel S10 value when published. |
| `retrieved_at` | Import retrieval timestamp. |
| `import_audit_id` | Import job/audit record ID. |
| `parser_version` | Parser implementation version. |
| `source_hash` | Hash of downloaded source body or file. |
| `freshness_status` | `CURRENT`, `STALE`, `FAILED`, or `UNKNOWN`. |
| `confidence` | Confidence value from parser/manual approval. |
| `approval_status` | `STAGED`, `APPROVED`, `REJECTED`, `SUPERSEDED`. |

Coefficient row:

| Field | Purpose |
|---|---|
| `source_period_id` | Link to source period. |
| `table_label` | Source table label, such as `A`, `B`, `C`, or `D`. |
| `operation_type` | Lotacao, fracionada, TAC-agregado, or source-original mapped value. |
| `cargo_type` | Canonical cargo type plus source-original label. |
| `vehicle_axle_count` | Loaded axle count or source-original axle band. |
| `vehicle_assumption` | High-performance, standard, own motor vehicle, or source-original assumption. |
| `coefficient_cost_per_km` | Decimal coefficient when source exposes it. |
| `coefficient_load_unload` | Decimal fixed/load/unload coefficient when source exposes it. |
| `minimum_amount_formula` | Versioned formula reference, not free text. |
| `raw_row_text` | Raw row or cell text for audit. |
| `row_hash` | Stable row fingerprint. |

Checker record:

| Field | Purpose |
|---|---|
| `offered_freight` | Driver-entered freight amount. |
| `distance_km` | Loaded or relevant distance assumption used in the check. |
| `cargo_type` | Driver-selected cargo type. |
| `operation_type` | Driver-selected operation type. |
| `vehicle_axle_count` | Driver-selected loaded axle count. |
| `floor_amount` | Calculated advisory minimum when known. |
| `margin_amount` | Offered freight minus floor amount. |
| `status` | `above_floor`, `below_floor`, or `unknown`. |
| `source_period_id` | Active source period used. |
| `confidence` | Source confidence. |
| `advisory_text` | Required non-certification wording. |

### Manual/Admin Fallback

Use manual fallback when:

- the latest ANTT page links only to a calculator or non-tabular format;
- PDF extraction changes row structure;
- DOU HTML changes table labels or coefficient labels;
- official publication has correction/retification notes;
- parser detects missing table groups, axle ranges, effective dates, or cargo labels;
- legal/operational wording changes CIOT blocking semantics.

Manual fallback workflow:

1. Admin creates a source period with official URL, publication ID, effective date, source hash, and notes.
2. Admin uploads or enters rows from the official publication.
3. System runs deterministic validation and synthetic fixture scenarios.
4. A second reviewer approves or rejects the period.
5. Active checker returns `unknown` for unmatched cargo/operation/axle combinations instead of guessing.

### Test Fixture Strategy

- Use synthetic fixtures shaped like ANTT/DOU tables; do not copy full official tables into the repo unless licensing and reuse are explicitly cleared.
- Include one fixture per supported parser adapter.
- Include format-change fixtures that must fail safely.
- Include exact expected values for:
  - above-floor scenario;
  - below-floor scenario;
  - unknown cargo type;
  - unknown operation type;
  - missing axle row;
  - stale source period;
  - unapproved source period.
- Store fixture README metadata: source shape, synthetic status, parser version, and expected confidence.

### Deferral Decision

Production automation is deferred until a follow-up implementation proves:

- stable retrieval from official ANTT/DOU source;
- deterministic parser tests with synthetic fixtures and at least one manually reviewed official-source sample outside the repository;
- approval workflow for source periods;
- checker output includes source, source period, retrieved-at, confidence, and advisory wording;
- no copy implies Cofrete provides legal certification or official ANTT validation.

Until then, the product may show `unknown` with official-source links, or use a manually approved source period with `manual_official_source_reviewed` confidence.
