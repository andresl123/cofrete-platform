# Auth

## Product Rule

Cofrete app login is separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, or state systems.

## Roles

- Driver: owns personal trucker workflows and documents.
- Company admin: manages small fleet/company records when enabled.
- Support: helps debug imports and support cases with restricted access.
- Platform admin: operational configuration and elevated support.

## Token And Session Expectations

- Mobile and web authenticate against Core API.
- Access tokens should carry principal type and authorized account scope.
- Service-to-service communication should use internal credentials with least privilege.
- Support/admin access must be auditable.

## Recommended MVP Login Model

Use Core API as the application auth boundary. The MVP may start with email/password auth or a conventional identity provider, but the implementation must keep the same API-level contract:

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

Recommended defaults:

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

- Drivers can access only their own account, profile, trucks, trips, documents, receivables, reserves, and compliance records.
- Company admins can access only records attached to their company account when that mode is enabled.
- Support users must have restricted read/support actions and auditable access.
- Platform admins can manage operational configuration, but elevated actions must be logged.
- API responses must distinguish unauthenticated requests from authenticated-but-forbidden requests.

## Service-To-Service Access

Workers do not use driver or web-admin login sessions. `finance-worker` and `data-importer-worker` should authenticate as internal services using dedicated credentials with least privilege.

This means:

- `finance-worker` can consume finance events and write calculation results, but it should not get broad profile, document, or admin permissions.
- `data-importer-worker` can write import audit records and imported datasets, but it should not read driver private documents or act as a driver.
- Internal credentials must be rotated and stored through environment variables or a secret manager, never committed.
- Service actions should be traceable with service identity, event ID, and correlation ID.

## Gov.br Boundary

Cofrete may show instructions and links for RNTRC Digital or official portals. It must not ask for, store, proxy, or reuse gov.br credentials.

## Linear Tracking

Authentication and access-control implementation scope is tracked by [ROU-245 / COF-010A](https://linear.app/routing-worker/issue/ROU-245/cof-010a-define-authentication-and-access-control-implementation-scope).
