# Testing Policy

## Always Test

- Finance calculations.
- Reserve allocations.
- Toll and Vale-Pedagio classification.
- Import parsing and freshness logic.
- Auth and authorization.
- Compliance wording variants that affect user decisions.

## Test Types

- Unit tests for deterministic domain math.
- Integration tests for persistence, API validation, messaging, and imports.
- Contract tests when API/event shapes become shared.
- Mobile/web tests for rendering, validation, empty states, and error states.
- Smoke tests for demo and MVP release flows.

## Finance Rule

Financial tests must use exact expected values, explicit currency, and named assumptions. Do not rely on floating-point comparisons for money.
