# Service Boundaries

## Core API

Owns:

- Users, drivers, trucks, trailers, and tax profiles.
- Freights, trips, direct costs, refuels, toll records, and Vale-Pedagio records.
- Reserve buckets, reserve transactions, customers, receivables, and documents.
- Compliance metadata, CIOT/freight-floor inputs, waiting-time records, IPVA/licensing reminders, and mobile/web authentication.
- Auth endpoints, Spring Security configuration, session issuance/revocation, user role authorization, account scoping, API request validation, and persistence.
- Audit logging for user/admin/support HTTP actions and internal service writes that land in Core API.
- Temporary MVP synchronous reserve allocation through ROU-217, so mobile and dashboard work can consume durable reserve wallets before the async worker result path lands in ROU-253.

Internal implementation rule:

When implementation begins, `core-api` must be internally modular. Keep clear package/module boundaries for:

- `profile`
- `trip`
- `finance`
- `reserve`
- `compliance`
- `receivables`
- `imports`
- `auth`
- `audit`

These modules should be separated in code even while they deploy together inside `core-api`. This keeps the first implementation simple to operate while preserving a clean path to extract high-pressure domains into independent services later.

Does not own:

- Long-running external imports.
- Heavy financial recalculation loops.
- Frontend-specific business math.
- Long-term reserve allocation math after ROU-253 wires worker-owned async allocation result persistence.

## Finance Worker

Owns:

- Trip profitability calculations.
- Reserve allocation calculations.
- Safe-withdrawal calculations.
- Fuel estimate calculations from route km, consumption, diesel price, and confidence.
- Waiting-time financial impact and customer profitability recalculation.
- Financial health scoring.
- Idempotent handling of finance events.

ROU-217 introduces deterministic reserve allocation logic in the worker and a temporary synchronous Core API allocation path for MVP persistence. ROU-253 is the follow-up that makes the documented async ownership fully effective by persisting worker allocation results back into Core API wallets and transactions.

Does not own:

- User authentication.
- User refresh sessions or user role assignment.
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
- User refresh sessions or user role assignment.

## Mobile App

Owns driver-facing workflows, validation presentation, navigation, offline/error states, platform secure storage usage for mobile auth material, and API client usage. Business math must stay in backend/domain code.

## Web App

Owns admin/backoffice workflows, demo visibility, support screens, operational dashboards, and browser client behavior for Core API auth. Web admin should prefer secure, HTTP-only, SameSite cookies for refresh sessions when the deployment shape supports it.

## Shared Infrastructure Access

- PostgreSQL access should use service-specific credentials for Core API, finance worker, and data importer worker.
- RabbitMQ publishers and consumers should use service-specific credentials limited to required exchanges and queues.
- Object storage access for user documents should be mediated by Core API or signed URLs unless a worker has a documented, least-privilege need.
- Internal service credentials are separate from user login sessions and must be stored through environment variables or a secret manager, never committed.
