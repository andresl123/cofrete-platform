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
- Keep source-review metadata for mandatory-insurance assumptions: requirement scope, policy type, source name, source URL, effective dates, reviewed timestamp, freshness, and confidence.
- Distinguish driver-entered policy records from source-backed regulatory/professional guidance.

## ROU-251 Source Refresh Workflow

Use `InsuranceRequirementRule` records for source-review metadata. These records do not certify coverage and do not recommend policies.

Required review checklist before changing mandatory-insurance guidance:

- Re-check SUSEP source pages and relevant CNSP/SUSEP norm references.
- Re-check ANTT/RNTRC insurance integration notes when policy linkage affects RNTRC guidance.
- Confirm with insurer/broker/professional guidance before translating a regulatory assumption into driver-facing wording.
- Record effective date, reviewed timestamp, source URL, freshness status, and confidence.
- Mark source status as `STALE` or `UNKNOWN` when source age, applicability, or interpretation cannot be proven.

Core API workflow:

- `POST /api/source-rules/insurance-requirements` creates or updates reviewed source metadata.
- `GET /api/source-rules/insurance-requirements` returns the effective source rule or a conservative missing status.
- Insurance policy responses include `sourceReview` metadata while policy details remain driver-entered metadata.
- Expiration alerts include source status, source URL, reviewed timestamp, confidence, and conservative wording when the source status is missing, stale, failed, or unknown.

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

IF mandatory-insurance source metadata is missing, stale, failed, or unknown:
    Keep alert wording conservative and tell the driver to confirm with SUSEP, insurer, broker, or qualified professional.
```

The master blueprint flags ANTT/RNTRC mandatory-insurance integration as a production-sensitive area. Validate exact dates, data-exchange behavior, and required policy types against official ANTT/SUSEP sources before launch.

## Wording

- "Track your insurance policy information for organization and reminders."
- "Confirm required coverage with your insurer, broker, or qualified professional."
- "Cofrete is not an insurance channel and does not certify coverage."
- "Mandatory insurance is not only a cost. It is also part of your transport compliance."
