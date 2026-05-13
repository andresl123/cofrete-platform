# Temporary Core API Reserve Allocation

ROU-217 keeps reserve allocation synchronous in Core API so reserve wallets, transaction history, and safe-withdrawal responses are immediately durable for MVP mobile and dashboard work. This deliberately differs from the long-term boundary where Finance Worker owns reserve allocation math; ROU-253 will wire the async event/result persistence path and remove duplicated Core API math from the final flow.
