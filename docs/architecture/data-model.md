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
- IpvaRule: state, vehicle type, rate percent, effective year, and source URL.

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
- TruckConsumptionProfile: truck-specific loaded/empty km-per-liter averages, source window, confidence, and last calculation timestamp.

## Reserves

- ReserveBucket: named bucket, target, current balance, and policy metadata.
- ReserveTransaction: credit/debit movement, source event, and audit metadata.
- ReserveRule: category, percentage, fixed monthly amount, per-km amount, effective dates, and source assumptions.

## Brazil Data

- FuelPrice: ANP/imported fuel price by product, state, city, period, source, and confidence.
- FuelStationPrice: optional station-level fuel price from official or partner datasets when source terms allow.
- DriverFuelReport: driver-confirmed fuel price report with receipt reference.
- TollPlaza: imported toll plaza metadata, highway, location, direction, and source.
- TollTariff: toll amount by plaza, vehicle category, axle count, and effective date.
- TripToll: estimated/actual toll for a trip and reimbursement classification.
- ValePedagioRecord: pass-through payment proof and classification.

## Compliance

- RntrcProfile: number, category, status, source, and last checked date.
- InsurancePolicy: RCTR-C, RC-DC, RC-V, truck hull, life/accident, insurer, broker/contact, policy number, dates, annual premium, monthly reserve, linked RNTRC flag, PGR requirement flag, verification status, and document reference.
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

Follow-up migration sets should include tables equivalent to:

- `fuel_prices`
- `fuel_station_prices`
- `driver_fuel_reports`
- `toll_plaza`
- `toll_tariff`
- `trip_toll`
- `trip_profitability_snapshots`
- `freight_floor_checks`
- `waiting_time_records`

## Audit Fields

Every financial, import, compliance, and document record should include created/updated timestamps. Calculation and import outputs should store enough input metadata to explain how a value was produced.
