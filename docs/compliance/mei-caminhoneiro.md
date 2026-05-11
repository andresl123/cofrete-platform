# MEI Caminhoneiro

Cofrete supports MEI Caminhoneiro as a tax profile mode for advisory tracking.

## App Behavior

- Store tax profile type and year-to-date revenue metadata.
- Track annual revenue limit usage.
- Alert when revenue approaches review thresholds.
- Reserve expected DAS/tax amounts when configured.

## Warning Bands

| Band | Rule |
|---|---|
| Green | Below 70% of configured annual limit |
| Yellow | 70% to 90% |
| Orange | 90% to 100% |
| Red | Above configured limit; recommend accountant review |

## Wording

- "This is an estimate for organization, not tax advice."
- "Confirm MEI rules and limits with official Receita Federal/Portal do Empreendedor sources or an accountant."

Official links are listed in `docs/compliance/official-source-register.md`.
