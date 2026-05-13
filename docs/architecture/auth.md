# Auth

## Product Rule

Cofrete app login is separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, or state systems. Cofrete must not ask for, store, proxy, or reuse official-portal credentials.

## Roles

- `DRIVER`: owns personal trucker workflows and documents.
- `COMPANY_ADMIN`: reserved for post-MVP company mode; manages small fleet/company records only after a future issue enables company workflows.
- `SUPPORT`: helps debug imports and support cases with restricted access.
- `PLATFORM_ADMIN`: manages operational configuration and elevated support.

Internal services are authenticated separately from user roles:

- `SERVICE_FINANCE_WORKER`: consumes finance events and writes finance calculation outputs.
- `SERVICE_DATA_IMPORTER_WORKER`: imports external datasets and writes import audit/status records.

## Selected MVP Implementation Decision

Core API is the application auth boundary for the MVP. Scaffold `core-api` with Spring Security and a Cofrete-owned email/password login flow first. A conventional identity provider can replace password verification later, but mobile and web clients should keep using the same Core API auth endpoints.

MVP session model:

- Access tokens are short-lived bearer tokens for API calls. The scaffold may use signed JWTs or opaque tokens, but the API contract must not expose sensitive domain data in the token.
- Refresh sessions are server-side records or rotation-capable refresh tokens. They must be individually revocable.
- Passwords, if stored directly by Cofrete, must be hashed with Argon2id or bcrypt. Plaintext passwords must never be stored or logged.
- Local development may seed synthetic users only. Production secrets, password hashes, refresh token material, signing keys, and service credentials must stay out of Git.
- Future external identity providers must not be gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, or other official-portal credential reuse.

## Token And Session Expectations

- Mobile and web authenticate against Core API.
- Access tokens should carry only authorization metadata: principal ID, principal type, role names, authorized account/company scope, issued-at time, expiry, and token ID.
- Tokens must not carry CPF/CNPJ, truck plate, RENAVAM, RNTRC, document content, freight values, receivable values, bank/payment data, or compliance document data.
- Refresh sessions must be revoked on logout, suspicious access, password reset, support/admin access changes, and explicit account disablement.
- Service-to-service communication must use internal credentials with least privilege.
- Support/admin access must be auditable.

## Principal And Account Scope

Every authenticated request resolves to one principal:

```json
{
  "id": "user_123",
  "type": "DRIVER",
  "accountId": "acct_123",
  "companyId": null,
  "roles": ["DRIVER"]
}
```

Rules:

- `id` is the authenticated user or internal service ID.
- `type` is one of `DRIVER`, `COMPANY_ADMIN`, `SUPPORT`, `PLATFORM_ADMIN`, `SERVICE_FINANCE_WORKER`, or `SERVICE_DATA_IMPORTER_WORKER`.
- `accountId` is required for driver-owned data and is the default row-level scope for profile, truck, trip, finance, reserve, receivable, document, and compliance records.
- `companyId` is present only for company-mode records when that mode is enabled.
- `COMPANY_ADMIN` must not be active or assignable during the single-driver MVP. It is reserved so the auth model can support future company mode without redesign.
- Support and platform-admin requests that inspect a driver or company account must include an explicit target account in the request path or query and must produce an audit entry.
- Internal service principals must not act as a driver, company admin, support user, or platform admin.

## Recommended MVP Login Model

Use Core API as the application auth boundary. The MVP starts with Cofrete-owned email/password auth behind Core API endpoints. If a conventional identity provider is introduced later, it must keep the same API-level contract:

```text
mobile-app / web-app
        |
        v
core-api auth endpoints
        |
        v
short-lived access token + refresh session
        |
        v
authenticated Cofrete APIs
```

Scaffold defaults:

- Use Spring Security in `core-api`.
- Use short-lived access tokens for API calls.
- Use refresh-token rotation or server-side refresh sessions.
- Hash passwords with a current password hashing algorithm such as Argon2id or bcrypt when Cofrete stores passwords directly.
- Store mobile credentials/tokens only in platform secure storage.
- Prefer secure, HTTP-only, SameSite cookies for the web admin surface when deployment shape allows it.
- Include principal ID, principal type, account scope, issued-at time, expiry, and token ID in token/session metadata.
- Do not put CPF/CNPJ, truck plate, RNTRC, document content, or financial values inside tokens.
- Revoke refresh sessions on logout, suspicious access, password reset, and support/admin access changes.

## Auth API Surface

The implementation target for auth endpoints is documented in `docs/architecture/api-contracts.md`.

Minimum expected endpoints:

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET /api/auth/me
```

`POST /api/auth/register` creates the app identity only. Driver, truck, tax, and compliance profile data remain owned by the profile APIs.

## Authorization Rules

| Role | Allowed MVP Scope | Required Restrictions |
|---|---|---|
| `DRIVER` | Own account, profile, trucks, trips, documents, receivables, reserves, and compliance records | Cannot read or mutate another account or company record |
| `COMPANY_ADMIN` | Reserved for post-MVP company mode; records attached to the company account only after a future issue enables company workflows | Must not be active or assignable during the single-driver MVP; cannot access unrelated company or driver personal records |
| `SUPPORT` | Restricted support reads and explicitly allowed support actions for a target account | Cannot perform financial, document, compliance, or identity mutations unless a specific implementation issue allows the action |
| `PLATFORM_ADMIN` | Operational configuration and elevated support workflows | Must log elevated access and should not bypass account-scoped APIs silently |
| `SERVICE_FINANCE_WORKER` | Finance event consumption and finance result writes | Cannot authenticate to user-facing APIs as a user or read private documents broadly |
| `SERVICE_DATA_IMPORTER_WORKER` | Import dataset writes, import audit records, and freshness/status updates | Cannot act as a driver or mutate driver-entered finance data |

API responses must distinguish unauthenticated requests from authenticated-but-forbidden requests:

- Return `UNAUTHENTICATED` when credentials are missing, expired, malformed, revoked, or invalid.
- Return `FORBIDDEN` when credentials are valid but the principal lacks the role, account scope, company scope, or service permission.

## Audit Logging Requirements

Audit entries are required for:

- Support or platform-admin access to driver, company, document, financial, compliance, or identity records.
- Financial changes including receivables, reserve rules, reserve allocations, expenses, profitability snapshots, and financial health overrides.
- Document upload, replacement, deletion, download, and signed URL issuance.
- Compliance changes including RNTRC, insurance, IPVA/licensing, tax profile, CIOT, freight-floor, and waiting-time records.
- Authentication-sensitive changes including login failures that trigger throttling, password resets, refresh-session revocation, role changes, and account disablement.
- Internal service writes from workers.

Minimum audit metadata:

- actor type, actor ID, roles, and service name when applicable
- target account/company/user when applicable
- action, resource type, resource ID, outcome, and failure reason when applicable
- correlation ID plus event ID for asynchronous worker actions
- request IP and user agent for user-facing HTTP actions when available
- before/after metadata for sensitive state transitions, excluding secrets and full document content

## Service-To-Service Access

Workers do not use driver or web-admin login sessions. `finance-worker` and `data-importer-worker` should authenticate as internal services using dedicated credentials with least privilege.

This means:

- Core API remains the only public HTTP auth boundary.
- `finance-worker` can consume finance events and write calculation results, but it should not get broad profile, document, or admin permissions.
- `data-importer-worker` can write import audit records and imported datasets, but it should not read driver private documents or act as a driver.
- PostgreSQL must use distinct application roles for Core API, finance worker, and data importer worker when production privileges are configured.
- RabbitMQ must use distinct users or credentials for publishers and consumers, limited to the exchanges and queues each service needs.
- Object storage access must be mediated by Core API for user documents unless a worker has a specific documented need. Worker credentials should be prefix- or bucket-scoped.
- Internal credentials must be rotated and stored through environment variables or a secret manager, never committed.
- Service actions should be traceable with service identity, event ID, and correlation ID.

## Gov.br Boundary

Cofrete may show instructions and links for RNTRC Digital or official portals. It must not ask for, store, proxy, or reuse gov.br credentials.

## Linear Tracking

Authentication and access-control implementation scope is tracked by [ROU-245 / COF-010A](https://linear.app/routing-worker/issue/ROU-245/cof-010a-define-authentication-and-access-control-implementation-scope).
