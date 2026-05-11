# Tax Profiles

Cofrete stores tax profile metadata so calculations can reserve money for likely obligations. It does not provide tax advice.

## Initial Profiles

- MEI Caminhoneiro.
- Pessoa Fisica autonoma.
- ME or small company.
- Ltda.
- Cooperative.
- Unknown/manual.

## Rules

- Tax calculations must be configurable and visible.
- Warnings should recommend accountant review instead of making legal conclusions.
- Financial reports must label assumptions and source dates.
- Any automatic tax estimate must be tested and documented.
- Tax profile rules must be versioned by year/source when a numeric value affects a calculation.

## Pessoa Fisica Autonoma

The master blueprint includes a planning assumption for cargo transport by autonomous individuals: track gross receipts and expose the taxable portion as a configurable tax-planning rule. The initial rule can represent the common planning split where a percentage of cargo transport gross receipts is treated as taxable service income and the rest is non-taxable, but the value must be configurable and source-linked.

```text
pf_tax_rule:
  activity: "cargo_transport"
  taxable_percent: configured value
  effective_year: 2026
  source_url: "..."
```

The UI must label this as an estimate and route the driver to Receita Federal/accountant review before filing or changing tax regime.

## Wording

- "Tax values are estimates for planning."
- "Confirm tax obligations with Receita Federal, state/municipal authorities, and a qualified accountant."
