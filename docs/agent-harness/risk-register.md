# Risk Register

| Area | Risk | Control |
|---|---|---|
| Finance calculations | Wrong safe-withdrawal result can mislead drivers | Deterministic tests and calculation trace |
| Taxes and compliance | App may be mistaken for legal/tax advice | Advisory wording and official-source links |
| Toll handling | Reimbursement counted as profit | Explicit toll classification |
| Diesel prices | Stale or inaccurate price changes estimates | Source confidence and freshness metadata |
| RNTRC/insurance | App may imply official verification | Separate app reminders from official action |
| RNTRC automation | Public consultation automation may violate source terms, break under anti-abuse controls, or imply official status | MVP uses manual/link-only workflow; no gov.br credentials; automation deferred until source/API terms and legal review are approved |
| Insurance source freshness | Stale SUSEP/insurance guidance could make mandatory-insurance alerts too confident | Source-review metadata with URL, reviewed timestamp, freshness, confidence, and conservative stale/unknown wording |
| CIOT/freight floor | App may imply a legal certification or use stale ANTT tables | Advisory wording, source dates, and importer tests before automation |
| IPVA/licensing | Hardcoded national rules would be wrong by state/year | State/year rule tables with official-source links |
| Loading/unloading waiting time | Wrong threshold or rate could create customer disputes | Configurable source-backed rule and legal review before production copy |
| gov.br/ANTT login | Asking for government credentials would create trust and security risk | App auth stays separate; never request gov.br credentials |
| User documents | Sensitive files exposed | Authenticated access and no raw logs |
| Receipts/images | Storage and access-control risk | Signed URLs or authenticated APIs |
| Payment data | Financial privacy risk | Least privilege and audit logging |
| Driver location | Future safety/privacy risk | Review before adding real-time location |

Review this register before launch and when a PR touches high-risk areas.

MVP launch is blocked unless `docs/runbooks/mvp-launch.md` checklist and COF-022 smoke validation are green or explicitly waived by a release owner.
