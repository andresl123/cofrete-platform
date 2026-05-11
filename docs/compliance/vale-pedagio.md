# Vale-Pedagio And Tolls

Vale-Pedagio and toll reimbursements are pass-through or receivable money. They must not inflate freight profit.

## Finance Rules

| Classification | Meaning | Finance Treatment |
|---|---|---|
| `VALE_PEDAGIO_RECEIVED` | Contractor provided toll payment in advance | Pass-through, not income |
| `DRIVER_PAID_MANUALLY` | Driver paid tolls with own money | Receivable/reimbursement |
| `VALE_PEDAGIO_NOT_CONFIRMED` | Proof not provided yet | Pre-trip alert |
| `TOLL_INCLUDED_IN_FREIGHT` | Payer says toll is included in freight | Warning, may distort profit |
| `NO_TOLL_ON_ROUTE` | Route appears to have no tolls | No toll cost |
| `UNKNOWN` | App cannot validate toll status | Manual review needed |

## Product Wording

- "Vale-Pedagio is pass-through money and is not counted as your profit."
- "If you paid tolls yourself, track the amount as reimbursement to collect."
- "Confirm official Vale-Pedagio rules with ANTT or qualified support before production decisions."

## Official Sources

See `docs/compliance/official-source-register.md`.
