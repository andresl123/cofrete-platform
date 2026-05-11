# Risk Register

| Area | Risk | Control |
|---|---|---|
| Finance calculations | Wrong safe-withdrawal result can mislead drivers | Deterministic tests and calculation trace |
| Taxes and compliance | App may be mistaken for legal/tax advice | Advisory wording and official-source links |
| Toll handling | Reimbursement counted as profit | Explicit toll classification |
| Diesel prices | Stale or inaccurate price changes estimates | Source confidence and freshness metadata |
| RNTRC/insurance | App may imply official verification | Separate app reminders from official action |
| User documents | Sensitive files exposed | Authenticated access and no raw logs |
| Receipts/images | Storage and access-control risk | Signed URLs or authenticated APIs |
| Payment data | Financial privacy risk | Least privilege and audit logging |
| Driver location | Future safety/privacy risk | Review before adding real-time location |

Review this register before launch and when a PR touches high-risk areas.
