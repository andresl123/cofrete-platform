# RNTRC And ANTT

Cofrete tracks RNTRC metadata for advisory organization. It does not create, update, certify, or replace official RNTRC records.

## Product Wording

- "RNTRC is your official ANTT registration for paid cargo transportation in Brazil."
- "To update your RNTRC, access RNTRC Digital using your gov.br account, level prata or ouro."
- "Your Cofrete profile is not an official ANTT record. Official updates must be done through ANTT/RNTRC Digital."

## App Behavior

- Store RNTRC number, category, status, and last checked date.
- Distinguish public consultation from official update actions.
- Link users to official ANTT channels.
- Use careful warning language: "may affect compliance" and "check official ANTT channels."
- Do not call gov.br an "ANTT account."
- App login stays separate from government login. Cofrete must never request or store gov.br credentials.

## Status Logic

```text
IF driver has no RNTRC number:
    Ask the driver to add RNTRC metadata for compliance monitoring.

IF RNTRC status is unknown:
    Show "Unknown - check official ANTT consultation."

IF RNTRC status is inactive, suspended, expired, or cancelled:
    Warn that it may affect paid cargo transport and link to official ANTT channels.
```

## Official Sources

See `docs/compliance/official-source-register.md` for source URLs and last-check dates.
