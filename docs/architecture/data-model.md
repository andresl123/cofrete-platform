# Data Model

This document records the first implementation target. Profile-domain table names are now defined by Core API Flyway migrations; later domain tables will be defined by their implementation issues.

## Identity And Profile

- User: app identity and authentication subject.
- Driver: driver profile, contact metadata, CPF/CNPJ metadata, tax regime, state, RNTRC number, and active account link.
- Company: post-MVP small fleet or company account metadata, reserved until a future company-mode issue enables it.
- Truck: vehicle identifiers, plate, RENAVAM metadata, axle/category support.
- Trailer: optional trailer profile linked to trucks.
- TaxProfile: MEI Caminhoneiro, ME, Ltda, cooperative, or unknown profile metadata.
- TaxRuleYear: year-specific tax planning rule, annual limit, INSS/ISS/ICMS formula, effective dates, and source URL.
- IpvaRule: state, vehicle type, rate percent, effective year, IPVA/licensing source URLs, reviewed timestamps, and freshness status.

## Trip Economics

- Freight: customer-facing freight offer/payment metadata.
- Trip: route, dates, status, truck, driver, and freight association.
- TripExpense: direct cost, reimbursable flag, paid-by metadata, and receipt reference.
- Refuel: liters, price per liter, total paid, station metadata, and receipt reference.
- TripCostInput: fuel assumptions, direct costs, tolls, reserves, and payment timing.
- ProfitabilityEstimate: calculation input snapshot and output snapshot.
- TripProfitabilitySnapshot: persisted calculation output for auditability, including gross freight, diesel, ARLA, toll treatment, reserves, expected profit, safe withdrawal, margin, currency, and trace ID.
- FreightFloorCheck: manual or imported ANTT minimum estimate, input assumptions, margin, status, and source metadata.
- WaitingTimeRecord: loading/unloading arrival, release, excess hours or fraction, cargo tons, configured rate, amount owed, and customer impact.
- WaitingTimeRule: source-backed loading/unloading threshold hours, rate per ton-hour, effective dates, source URL, reviewed timestamp, freshness, and confidence.
- TruckConsumptionProfile: truck-specific loaded/empty km-per-liter averages, source window, confidence, and last calculation timestamp.

## Reserves

- ReserveBucket: named bucket, target, current balance, and policy metadata.
- ReserveTransaction: credit/debit movement, source event, and audit metadata. ROU-217 implements positive credit movements; debit and adjustment corrections need a later documented request shape.
- ReserveRule: category, percentage, fixed monthly amount, per-km amount, effective dates, and source assumptions.

## Brazil Data

- FuelPrice: ANP/imported fuel price by product, state, city, period, source, and confidence.
- FuelStationPrice: optional station-level fuel price from official or partner datasets when source terms allow.
- DriverFuelReport: driver-confirmed fuel price report with receipt reference.
- TollPlaza: imported toll plaza metadata, highway, location, direction, and source.
- TollTariff: toll amount by plaza, vehicle category, axle count, and effective date.
- TripToll: estimated/actual toll for a trip and reimbursement classification.
- ValePedagioRecord: pass-through payment proof and classification.
- FreightFloorSourcePeriod: official-source period for ANTT minimum freight-floor coefficients, including source URL, publication ID, effective dates, parser version, source hash, import audit ID, approval status, freshness, and confidence.
- FreightFloorCoefficient: source-backed coefficient row by table label, cargo type, operation type, vehicle/axle assumption, source-original labels, formula version, row hash, and raw row text.
- FreightFloorCheck: advisory check result for offered freight against a source period, returning `above_floor`, `below_floor`, or `unknown` with margin, source, source period, and confidence.

## Compliance

- RntrcProfile: number, category, status, source, and last checked date.
- InsurancePolicy: RCTR-C, RC-DC, RC-V, truck hull, life/accident, insurer, broker/contact, policy number, dates, annual premium, monthly reserve, linked RNTRC flag, PGR requirement flag, verification status, and document reference.
- InsuranceRequirementRule: mandatory-insurance source review metadata by requirement scope and policy type, including source name, source URL, effective dates, reviewed timestamp, freshness, confidence, required flag, and notes.
- ComplianceDocument: document metadata, expiration, object storage reference.
- ComplianceAlert: advisory alert with source and severity.
- ComplianceScoreSnapshot: weighted score components for RNTRC, mandatory insurance, expired documents, IPVA/licensing, and CIOT/Vale-Pedagio tracking.
- MaintenanceRecord: maintenance date, system, workshop, cost, and odometer metadata.
- TireRecord: tire position, cost, installed/removed odometer, and lifecycle metadata.

## Receivables

- Customer: freight payer or customer profile.
- Receivable: expected, paid, late, partially paid, or canceled payment.
- Payment: freight payment amount, due date, paid date, method, and status.

## Imports

- ImportJob: source, dataset, status, started/completed timestamps, row count, freshness status, file/hash metadata, and error summary.

## First SQL Targets

The implemented Core API profile migration includes:

- `app_users`
- `drivers`
- `trucks`
- `trailers`
- `tax_profiles`
- `tax_rule_years`
- `ipva_rules`
- `compliance_profiles`
- `insurance_policies`

The implemented Core API reserve migration includes:

- `reserve_rules`
- `reserve_wallets`
- `reserve_allocations`
- `reserve_transactions`

The implemented Core API trip profitability migration includes:

- `freights`
- `trips`
- `trip_cost_inputs`
- `profitability_snapshots`
- `trip_acceptance_decisions`
- `trip_recalculation_events`

The implemented Core API fuel, toll, and source-rule migrations include:

- `import_jobs`
- `fuel_prices`
- `driver_fuel_reports`
- `truck_consumption_profiles`
- `toll_plazas`
- `toll_tariffs`
- `freight_floor_source_periods`
- `freight_floor_coefficients`
- `freight_floor_checks`
- `waiting_time_rules`
- `insurance_requirement_rules`

Reserve allocation records keep nullable `tripId` and `freightPaymentId` references until a later migration links reserve allocation subjects to trip and receivable records without disrupting existing allocation idempotency keys.

Follow-up migration sets should include tables equivalent to:

- `fuel_station_prices`
- `trip_toll`
- `waiting_time_records`

## Audit Fields

Every financial, import, compliance, and document record should include created/updated timestamps. Calculation and import outputs should store enough input metadata to explain how a value was produced.
