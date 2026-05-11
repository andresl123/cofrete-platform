# Harness Architecture Rules

- Service ownership is defined in `docs/architecture/service-boundaries.md`.
- API contracts are defined in `docs/architecture/api-contracts.md`.
- Event contracts are defined in `docs/architecture/event-contracts.md`.
- Compliance copy and official source links are defined in `docs/compliance/`.
- Product scope is defined in `docs/product/mvp-scope.md`.

## Non-Negotiables

- Controllers validate and delegate.
- Business rules live in domain/services, not UI components.
- Finance calculations are deterministic and auditable.
- Imported public data is normalized server-side.
- Gov.br and official government credentials are never handled by Cofrete.
- Toll reimbursement and Vale-Pedagio are never profit.
- MEI, IPVA, freight-floor, waiting-time, and tax planning values are configurable by source/year instead of hardcoded.
