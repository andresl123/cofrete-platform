CREATE TABLE freights (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    gross_freight numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    advance_amount numeric(14, 2),
    balance_due_days integer,
    cargo_description varchar(240),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE trips (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    truck_id varchar(64) NOT NULL REFERENCES trucks(id),
    freight_id varchar(64) NOT NULL UNIQUE REFERENCES freights(id),
    origin_city varchar(120) NOT NULL,
    origin_state varchar(2) NOT NULL,
    destination_city varchar(120) NOT NULL,
    destination_state varchar(2) NOT NULL,
    loaded_km numeric(14, 2) NOT NULL,
    empty_km numeric(14, 2) NOT NULL,
    expected_pickup_at timestamp with time zone,
    input_revision integer NOT NULL,
    decision_state varchar(40) NOT NULL,
    latest_snapshot_id varchar(64),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE trip_cost_inputs (
    id varchar(64) PRIMARY KEY,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    input_revision integer NOT NULL,
    diesel_consumption_km_per_liter numeric(12, 4) NOT NULL,
    diesel_price_per_liter numeric(12, 4) NOT NULL,
    arla_cost numeric(14, 2) NOT NULL,
    non_reimbursed_toll numeric(14, 2) NOT NULL,
    toll_reimbursement numeric(14, 2) NOT NULL,
    vale_pedagio numeric(14, 2) NOT NULL,
    other_pass_through numeric(14, 2) NOT NULL,
    meals_and_lodging_cost numeric(14, 2) NOT NULL,
    other_direct_cost numeric(14, 2) NOT NULL,
    financing_allocation numeric(14, 2) NOT NULL,
    maintenance_rate numeric(9, 6) NOT NULL,
    tire_rate numeric(9, 6) NOT NULL,
    tax_rate numeric(9, 6) NOT NULL,
    insurance_rate numeric(9, 6) NOT NULL,
    replacement_rate numeric(9, 6) NOT NULL,
    emergency_rate numeric(9, 6) NOT NULL,
    source_freshness varchar(500) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_trip_cost_inputs_trip_revision UNIQUE (trip_id, input_revision)
);

CREATE TABLE profitability_snapshots (
    id varchar(64) PRIMARY KEY,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    input_revision integer NOT NULL,
    calculation_trace_id varchar(80) NOT NULL,
    gross_freight numeric(14, 2) NOT NULL,
    pass_through_amount numeric(14, 2) NOT NULL,
    direct_trip_cost numeric(14, 2) NOT NULL,
    required_reserves numeric(14, 2) NOT NULL,
    safe_personal_withdrawal numeric(14, 2) NOT NULL,
    expected_profit numeric(14, 2) NOT NULL,
    margin_percent numeric(9, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    financial_health_status varchar(40) NOT NULL,
    recommendation varchar(40) NOT NULL,
    source_freshness varchar(500) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_profitability_snapshots_trip_revision UNIQUE (trip_id, input_revision)
);

ALTER TABLE trips
    ADD CONSTRAINT fk_trips_latest_snapshot
    FOREIGN KEY (latest_snapshot_id) REFERENCES profitability_snapshots(id);

CREATE TABLE trip_acceptance_decisions (
    id varchar(64) PRIMARY KEY,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    decision varchar(40) NOT NULL,
    reason_codes varchar(500),
    note varchar(500),
    decided_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE trip_recalculation_events (
    id varchar(64) PRIMARY KEY,
    event_type varchar(80) NOT NULL,
    version integer NOT NULL,
    event_id varchar(80) NOT NULL UNIQUE,
    idempotency_key varchar(160) NOT NULL UNIQUE,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    truck_id varchar(64) NOT NULL REFERENCES trucks(id),
    input_revision integer NOT NULL,
    reason varchar(60) NOT NULL,
    correlation_id varchar(160) NOT NULL,
    producer varchar(80) NOT NULL,
    requested_at timestamp with time zone NOT NULL,
    published_at timestamp with time zone NOT NULL
);

CREATE INDEX idx_trips_account_created ON trips(account_id, created_at);
CREATE INDEX idx_profitability_snapshots_trip_created ON profitability_snapshots(trip_id, created_at);
CREATE INDEX idx_trip_recalculation_events_trip_revision ON trip_recalculation_events(trip_id, input_revision);
