# ADR 0002: Temporary Core API Trip Profitability Estimates

## Status

Accepted

## Context

ROU-215 adds the trip-first MVP API workflow for the driver question: should I accept this freight?
The public contract includes a synchronous profitability estimate endpoint, durable trip/profitability records, and canonical `trip.recalculation.requested` publication.

Finance Worker owns long-term trip profitability calculation and already has deterministic calculation logic. The current platform does not yet have a shared calculation package or a worker result persistence path back into Core API snapshots.

## Decision

Core API may calculate and persist temporary synchronous trip profitability estimate snapshots for ROU-215.

The temporary Core API path must:

- keep the output advisory;
- exclude pass-through cash flow from profit and safe personal withdrawal;
- persist calculation trace IDs and input revisions;
- publish canonical `trip.recalculation.requested` events after durable state commits;
- remain scoped to the trip-first synchronous API workflow.

Finance Worker remains the long-term owner of async trip finance recalculation. Future worker result persistence should replace or supersede Core API-generated snapshots without changing the public monetary semantics.

## Consequences

The MVP mobile freight calculator can receive immediate API responses before asynchronous worker result persistence exists.

Core API temporarily duplicates a narrow slice of finance calculation behavior. Tests must cover exact money outputs and pass-through treatment until the worker-owned path becomes authoritative.

Future migration work must reconcile Core API estimate snapshots with Finance Worker recalculated snapshots idempotently by trip ID, input revision, and calculation trace ID.
