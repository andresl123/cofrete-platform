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

## Gov.br Boundary

Cofrete may show instructions and links for RNTRC Digital or official portals. It must not ask for, store, proxy, or reuse gov.br credentials.
