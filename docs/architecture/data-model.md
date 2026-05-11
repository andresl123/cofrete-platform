# Data Model

This document records the first implementation target. Exact table names and migrations will be defined in service scaffolding issues.

## Identity And Profile

- User: app identity and authentication subject.
- Driver: driver profile, contact metadata, CPF/CNPJ metadata, tax regime, state, RNTRC number, and active account link.
- Company: small fleet or company account metadata when enabled.
- Truck: vehicle identifiers, plate, RENAVAM metadata, axle/category support.
- Trailer: optional trailer profile linked to trucks.
- TaxProfile: MEI Caminhoneiro, ME, Ltda, cooperative, or unknown profile metadata.

## Trip Economics

- Freight: customer-facing freight offer/payment metadata.
- Trip: route, dates, status, truck, driver, and freight association.
- TripExpense: direct cost, reimbursable flag, paid-by metadata, and receipt reference.
- Refuel: liters, price per liter, total paid, station metadata, and receipt reference.
- TripCostInput: fuel assumptions, direct costs, tolls, reserves, and payment timing.
- ProfitabilityEstimate: calculation input snapshot and output snapshot.

## Reserves

- ReserveBucket: named bucket, target, current balance, and policy metadata.
- ReserveTransaction: credit/debit movement, source event, and audit metadata.

## Brazil Data

- FuelPrice: ANP/imported fuel price by product, state, city, period, source, and confidence.
- DriverFuelReport: driver-confirmed fuel price report with receipt reference.
- TollPlaza: imported toll plaza metadata, highway, location, direction, and source.
- TollTariff: toll amount by plaza, vehicle category, axle count, and effective date.
- TripToll: estimated/actual toll for a trip and reimbursement classification.
- ValePedagioRecord: pass-through payment proof and classification.

## Compliance

- RntrcProfile: number, category, status, source, and last checked date.
- InsurancePolicy: RCTR-C, RC-DC, RC-V, insurer, policy number, dates, and cost.
- ComplianceDocument: document metadata, expiration, object storage reference.
- ComplianceAlert: advisory alert with source and severity.
- MaintenanceRecord: maintenance date, system, workshop, cost, and odometer metadata.
- TireRecord: tire position, cost, installed/removed odometer, and lifecycle metadata.

## Receivables

- Customer: freight payer or customer profile.
- Receivable: expected, paid, late, partially paid, or canceled payment.
- Payment: freight payment amount, due date, paid date, method, and status.

## Imports

- ImportJob: source, dataset, status, started/completed timestamps, row count, and error summary.

## Audit Fields

Every financial, import, compliance, and document record should include created/updated timestamps. Calculation and import outputs should store enough input metadata to explain how a value was produced.
