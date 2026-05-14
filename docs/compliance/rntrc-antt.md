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
- Do not automate RNTRC public-status lookup for MVP. The public consultation page is user-facing and no stable, documented API or reuse terms have been approved for Cofrete automation.
- When a driver requests status help, return an advisory unsupported/manual-link response that points to ANTT public consultation and RNTRC Digital.

## ROU-250 Automation Recommendation

Recommendation: keep RNTRC status as manual app metadata and use a link-only official workflow for MVP.

Rationale reviewed on 2026-05-14:

- ANTT documents RNTRC Digital as the official update channel and requires a verified gov.br account at level prata or ouro for access.
- The public consultation page is available for user consultation, but Cofrete has not validated a stable machine-readable endpoint, rate/terms suitability, or anti-abuse constraints.
- Cofrete must not request, store, proxy, or reuse gov.br credentials.
- A stale or failed automated lookup could be mistaken for official ANTT status.

Allowed MVP workflow:

1. Driver enters RNTRC number/category/status or marks status as unknown.
2. Cofrete stores `source=driver_entered`, `lastCheckedAt` when the driver confirms a manual check, and `confidence=LOW` or equivalent until a reviewed source workflow exists.
3. Cofrete links to ANTT public consultation for status checking and RNTRC Digital for official updates.
4. Unknown, stale, inactive, suspended, expired, or cancelled statuses use conservative advisory wording.

Deferred automation gate:

- Documented ANTT source or API terms allow automated lookup.
- No gov.br credential handling is needed.
- Lookup failure, stale data, source version, last checked date, and confidence are exposed to users.
- Legal/compliance review confirms public consultation reuse is suitable.

## Status Logic

```text
IF driver has no RNTRC number:
    Ask the driver to add RNTRC metadata for compliance monitoring.

IF RNTRC status is unknown:
    Show "Unknown - check official ANTT consultation."

IF RNTRC status was last checked more than 90 days ago:
    Show "Stale - recheck official ANTT consultation."

IF RNTRC status is inactive, suspended, expired, or cancelled:
    Warn that it may affect paid cargo transport and link to official ANTT channels.
```

## Official Sources

See `docs/compliance/official-source-register.md` for source URLs and last-check dates.
