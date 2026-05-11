# Service Boundaries

## Core API

Owns:

- Users, drivers, trucks, trailers, and tax profiles.
- Freights, trips, direct costs, refuels, toll records, and Vale-Pedagio records.
- Reserve buckets, reserve transactions, customers, receivables, and documents.
- Compliance metadata and mobile/web authentication.
- API request validation and persistence.

Does not own:

- Long-running external imports.
- Heavy financial recalculation loops.
- Frontend-specific business math.

## Finance Worker

Owns:

- Trip profitability calculations.
- Reserve allocation calculations.
- Safe-withdrawal calculations.
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
- Import audit records, freshness status, retries, and failure reporting.

Does not own:

- User-facing finance calculations.
- Mobile/web authentication.

## Mobile App

Owns driver-facing workflows, validation presentation, navigation, offline/error states, and API client usage. Business math must stay in backend/domain code.

## Web App

Owns admin/backoffice workflows, demo visibility, support screens, and operational dashboards.
