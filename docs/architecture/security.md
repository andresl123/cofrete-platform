# Security

## Sensitive Data

Cofrete may handle CPF/CNPJ metadata, truck plate/RENAVAM, RNTRC number, insurance documents, receipts, freight values, customer payment history, document images, and future location data.

## Requirements

- Never commit real secrets.
- Use environment variables or secret managers for credentials.
- Use least-privilege service access.
- Do not log tokens, full document content, or unnecessary sensitive identifiers.
- Protect document downloads through authenticated APIs or signed URLs.
- Add audit logging for financial, document, and compliance changes.
- Encrypt production transport with TLS.
- Keep dev/demo data synthetic.
- Store password hashes only, never plaintext passwords.
- Keep user login sessions separate from internal worker credentials.
- Rotate refresh sessions and internal service credentials when compromise is suspected.
- Return `UNAUTHENTICATED` for missing/invalid login and `FORBIDDEN` for valid principals without resource access.

## Authentication And Authorization

- Core API owns user authentication, session issuance, session revocation, and request authorization.
- Use Spring Security in `core-api` for password authentication, bearer-token validation, role checks, and method/path authorization.
- Keep Cofrete login separate from gov.br, ANTT, RNTRC Digital, Receita Federal, ANP, SUSEP, insurer, DETRAN, SEFAZ, and state systems.
- Store only password hashes when Cofrete stores passwords directly. Use Argon2id or bcrypt, never plaintext or reversible encryption.
- Store refresh-session state server-side or use rotation-capable refresh tokens that can be revoked per session.
- Keep mobile tokens in platform secure storage.
- Prefer secure, HTTP-only, SameSite cookies for web admin refresh sessions when the deployment shape supports same-site web and API origins.
- Treat support/admin access as elevated access even when the user has a valid session.

## Access-Control Scope

- Account-scoped product APIs must authorize by authenticated principal and target account, not by request body ownership claims alone.
- Driver data, truck data, tax profiles, trips, expenses, receivables, reserves, documents, and compliance records are scoped to the owning account unless a later issue explicitly introduces shared ownership.
- Company-admin access is reserved for post-MVP company mode and must not be active or assignable until a future issue implements company workflows. When enabled, it is limited to records attached to the enabled company account.
- Support access must be restricted to support workflows and explicit target accounts.
- Platform-admin access must be reserved for operational configuration and explicitly allowed elevated workflows.
- Internal services must authenticate with service credentials and service roles. They must not use user refresh sessions or impersonate drivers/admins.

## Service Credentials

- Use separate credentials for Core API, finance worker, data importer worker, PostgreSQL, RabbitMQ, and object storage.
- PostgreSQL production permissions should use distinct least-privilege roles for each application service.
- RabbitMQ publishers and consumers should have distinct permissions for only the exchanges, queues, and routing keys they need.
- Object storage credentials should be scoped by bucket or prefix. User document access should go through authenticated Core API endpoints or signed URLs.
- Rotate internal credentials after suspected compromise, role changes, or deployment ownership changes.

## Audit Logging

Audit logs are required for support/admin account access, role changes, refresh-session revocation, password reset, account disablement, financial changes, document access or mutation, compliance changes, and worker writes.

Audit logs must capture actor, role or service identity, target account/company/user when applicable, action, resource type and ID, outcome, correlation ID, event ID for asynchronous actions, and request metadata when available. Do not store secrets, tokens, plaintext passwords, or full document contents in audit logs.

## Review Triggers

Security review is required when a change adds authentication, authorization, document storage, payment data, location data, import credentials, or support/admin access.
