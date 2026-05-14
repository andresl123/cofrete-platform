CREATE TABLE trip_tolls (
    id varchar(64) PRIMARY KEY,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    plaza_name varchar(160),
    amount numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    paid_by varchar(40) NOT NULL,
    classification varchar(40) NOT NULL,
    finance_treatment varchar(80) NOT NULL,
    confidence varchar(80) NOT NULL,
    source varchar(120),
    source_reference varchar(160),
    paid_at timestamp with time zone,
    note varchar(500),
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE vale_pedagio_records (
    id varchar(64) PRIMARY KEY,
    trip_id varchar(64) NOT NULL REFERENCES trips(id),
    provider varchar(160),
    proof_reference varchar(160),
    amount numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    received_status varchar(40) NOT NULL,
    classification varchar(40) NOT NULL,
    finance_treatment varchar(80) NOT NULL,
    confidence varchar(80) NOT NULL,
    received_at timestamp with time zone,
    note varchar(500),
    created_at timestamp with time zone NOT NULL
);

CREATE INDEX idx_trip_tolls_trip_created ON trip_tolls(trip_id, created_at);
CREATE INDEX idx_vale_pedagio_records_trip_created ON vale_pedagio_records(trip_id, created_at);
