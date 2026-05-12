# Service Boundaries

## Core API

Owns:

- Users, drivers, trucks, trailers, and tax profiles.
- Freights, trips, direct costs, refuels, toll records, and Vale-Pedagio records.
- Reserve buckets, reserve transactions, customers, receivables, and documents.
- Compliance metadata, CIOT/freight-floor inputs, waiting-time records, IPVA/licensing reminders, and mobile/web authentication.
- API request validation and persistence.

Internal implementation rule:

When implementation begins, `core-api` must be internally modular. Keep clear package/module boundaries for:

- `profile`
- `trip`
- `finance`
- `reserve`
- `compliance`
- `receivables`
- `imports`

These modules should be separated in code even while they deploy together inside `core-api`. This keeps the first implementation simple to operate while preserving a clean path to extract high-pressure domains into independent services later.

Does not own:

- Long-running external imports.
- Heavy financial recalculation loops.
- Frontend-specific business math.

## Finance Worker

Owns:

- Trip profitability calculations.
- Reserve allocation calculations.
- Safe-withdrawal calculations.
- Fuel estimate calculations from route km, consumption, diesel price, and confidence.
- Waiting-time financial impact and customer profitability recalculation.
- Financial health scoring.
- Idempotent handling of finance events.

Does not own:

- User authentication.
- Primary user profile writes.
- External source imports.

## Data Importer Worker

Owns:

- ANP diesel price imports.
- ANTT toll dataset imports when added.
- Optional future ANTT freight-floor table imports.
- Import audit records, freshness status, retries, and failure reporting.

Does not own:

- User-facing finance calculations.
- Mobile/web authentication.

## Mobile App

Owns driver-facing workflows, validation presentation, navigation, offline/error states, and API client usage. Business math must stay in backend/domain code.

## Web App

Owns admin/backoffice workflows, demo visibility, support screens, and operational dashboards.
