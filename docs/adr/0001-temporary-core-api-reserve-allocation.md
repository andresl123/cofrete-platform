# Temporary Core API Reserve Allocation

ROU-217 kept reserve allocation synchronous in Core API so reserve wallets, transaction history, and safe-withdrawal responses were immediately durable for MVP mobile and dashboard work.

ROU-253 supersedes that temporary path. Core API now records reserve allocation requests, publishes `reserve.allocation.requested` with deterministic input rules, and persists `reserve.allocation.completed` worker results idempotently into reserve wallets and transactions. Core API no longer owns reserve allocation math in the final async path.
