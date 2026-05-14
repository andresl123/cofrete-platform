# Vale-Pedagio And Tolls

Vale-Pedagio and toll reimbursements are pass-through or receivable money. They must not inflate freight profit.

## Finance Rules

| Classification | Meaning | Finance Treatment |
|---|---|---|
| `PASS_THROUGH` | Vale-Pedagio, toll reimbursement, or driver-paid toll expected as receivable/reimbursement | Pass-through, not income |
| `DRIVER_PAID_NON_REIMBURSED` | Driver paid tolls with own money and no reimbursement is expected | Direct trip cost that reduces profit |
| `INCLUDED_IN_FREIGHT` | Payer says toll is included in freight | Warning; exclude known toll component from profit when amount is identified |
| `NO_TOLL` | Route appears to have no tolls | No toll cost |
| `UNKNOWN` | App cannot validate toll status | Manual review needed |

Vale-Pedagio proof status is tracked separately from the toll classification:

| Vale-Pedagio Status | Meaning | Finance Treatment |
|---|---|---|
| `VALE_PEDAGIO_RECEIVED` | Contractor provided toll payment in advance | Pass-through, not income |
| `VALE_PEDAGIO_NOT_CONFIRMED` | Proof not provided yet | Pre-trip alert and manual review |

## Product Wording

- "Vale-Pedagio is pass-through money and is not counted as your profit."
- "If you paid tolls yourself, track the amount as reimbursement to collect."
- "Confirm official Vale-Pedagio rules with ANTT or qualified support before production decisions."

## Route-Based Toll Estimates

The free MVP architecture should use imported ANTT toll plaza data, OSRM/OpenStreetMap route geometry, and geospatial matching. ANTT open data can identify plazas and tariffs, but it does not by itself answer "origin + destination + vehicle + axles = exact toll cost."

Recommended detection flow:

```text
1. Get route polyline from OSRM or another route engine.
2. Buffer the route by 300 to 800 meters.
3. Search toll plazas inside the buffer.
4. Validate highway, direction, municipality, state, and km marker when available.
5. Apply truck category or axle count.
6. Calculate estimated toll total.
7. Store the result as pass-through or reimbursement, not profit.
```

Paid APIs such as Maplink, Google Routes, Rotas Brasil, or TollGuru can be evaluated later for production validation, coverage, and SLA.

## Official Sources

See `docs/compliance/official-source-register.md`.
