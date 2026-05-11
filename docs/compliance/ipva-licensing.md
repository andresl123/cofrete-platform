# IPVA And Licensing

IPVA, licensing, and vehicle document obligations vary by state and official channel.

## App Behavior

- Store state, due date, paid/unpaid status, and document metadata.
- Alert before expiration or due date.
- Reserve monthly amounts for expected obligations.
- Link users to the relevant state DETRAN/SEFAZ channel.
- Use state/year/vehicle-type rules when estimating IPVA; never use one national hardcoded truck percentage.

## Rule Storage

```text
ipva_rule:
  state: "GO"
  vehicle_type: "CAMINHAO"
  rate_percent: configured value
  effective_year: 2026
  source_url: "..."
```

The master blueprint includes example state rates only as planning examples. Treat them as source-backed records, not code constants.

## Wording

- "Cofrete helps organize reminders. Official values, due dates, and payment channels must be confirmed with your state authority."
- "Do not use Cofrete as proof of payment or licensing."

## Implementation Note

State-specific automation should start as manual metadata plus official-source links. Automated lookup should be introduced only after source reliability, terms, and privacy are reviewed.
