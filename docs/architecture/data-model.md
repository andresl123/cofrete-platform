# Data Model

This document records the first implementation target. Exact table names and migrations will be defined in service scaffolding issues.

## Identity And Profile

- User: app identity and authentication subject.
- Driver: driver profile, contact metadata, and active account link.
- Truck: vehicle identifiers, plate, RENAVAM metadata, axle/category support.
- TaxProfile: MEI Caminhoneiro, ME, Ltda, cooperative, or unknown profile metadata.

## Trip Economics

- Freight: customer-facing freight offer/payment metadata.
- Trip: route, dates, status, truck, driver, and freight association.
- TripCostInput: fuel assumptions, direct costs, tolls, reserves, and payment timing.
- ProfitabilityEstimate: calculation input snapshot and output snapshot.

## Reserves

- ReserveBucket: named bucket, target, current balance, and policy metadata.
- ReserveTransaction: credit/debit movement, source event, and audit metadata.

## Brazil Data

- FuelPrice: ANP/imported fuel price by product, state, city, period, source, and confidence.
- TollRecord: route toll estimate or manually entered toll.
- ValePedagioRecord: pass-through payment proof and classification.

## Compliance

- RntrcProfile: number, category, status, source, and last checked date.
- InsurancePolicy: RCTR-C, RC-DC, RC-V, insurer, policy number, dates, and cost.
- ComplianceDocument: document metadata, expiration, object storage reference.
- ComplianceAlert: advisory alert with source and severity.

## Receivables

- Customer: freight payer or customer profile.
- Receivable: expected, paid, late, partially paid, or canceled payment.

## Audit Fields

Every financial, import, compliance, and document record should include created/updated timestamps. Calculation and import outputs should store enough input metadata to explain how a value was produced.
