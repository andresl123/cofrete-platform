# Insurance

Cofrete tracks insurance metadata as finance and compliance support. It does not sell, recommend, certify, or replace insurance advice.

## Tracked Policies

- RCTR-C.
- RC-DC.
- RC-V.
- Truck hull insurance.
- Life/accident insurance.
- Other driver-entered policy metadata when useful.

## App Behavior

- Store insurer, policy number, policy type, effective date, expiration date, and cost.
- Store broker/contact, annual premium, monthly reserve, linked RNTRC flag, PGR requirement flag, verification status, and document reference.
- Alert before policy expiration.
- Include insurance reserve in finance calculations.
- Link to official SUSEP and insurer/professional guidance for decisions.
- Treat mandatory insurance as both a compliance requirement and a reserve requirement.

## Alert Logic

```text
IF RCTR-C is missing:
    Show advisory warning.

IF RC-DC is missing:
    Show advisory warning when the driver's profile/trip may require it.

IF RC-V is missing:
    Show advisory warning under the mandatory transport liability framework.

IF any mandatory insurance expires in 60 days:
    Show renewal planning alert.

IF any mandatory insurance expires in 30 days:
    Show high-priority alert.

IF any mandatory insurance is expired:
    Show compliance risk and recommend checking official, insurer, or broker channels.
```

The master blueprint flags ANTT/RNTRC mandatory-insurance integration as a production-sensitive area. Validate exact dates, data-exchange behavior, and required policy types against official ANTT/SUSEP sources before launch.

## Wording

- "Track your insurance policy information for organization and reminders."
- "Confirm required coverage with your insurer, broker, or qualified professional."
- "Cofrete is not an insurance channel and does not certify coverage."
- "Mandatory insurance is not only a cost. It is also part of your transport compliance."
