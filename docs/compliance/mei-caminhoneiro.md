# MEI Caminhoneiro

Cofrete supports MEI Caminhoneiro as a tax profile mode for advisory tracking.

## App Behavior

- Store tax profile type and year-to-date revenue metadata.
- Track annual revenue limit usage.
- Alert when revenue approaches review thresholds.
- Reserve expected DAS/tax amounts when configured.
- Store MEI rules by year instead of hardcoding current values.

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

## Rule Storage

MEI Caminhoneiro values can change by year, so the app should model them as data:

```text
tax_rule_year
regime
annual_limit
inss_formula
iss_amount
icms_amount
effective_start_date
effective_end_date
source_url
```

For planning, the master blueprint records MEI Caminhoneiro as a first-class MVP profile and Pessoa Fisica autonoma as the next profile to support. Do not hardcode the annual limit, DAS value, or minimum-wage-derived contribution in application logic.
