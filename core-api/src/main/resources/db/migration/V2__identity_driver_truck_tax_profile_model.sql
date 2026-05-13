CREATE TABLE app_users (
    id varchar(64) PRIMARY KEY,
    principal_name varchar(320) NOT NULL UNIQUE,
    user_type varchar(40) NOT NULL,
    role varchar(40) NOT NULL,
    account_id varchar(64) NOT NULL UNIQUE,
    company_id varchar(64),
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE drivers (
    id varchar(64) PRIMARY KEY,
    user_id varchar(64) NOT NULL UNIQUE REFERENCES app_users(id),
    name varchar(160) NOT NULL,
    email varchar(320),
    phone varchar(40),
    state varchar(2) NOT NULL,
    document_type varchar(20),
    cpf_cnpj_last4 varchar(4),
    rntrc_number varchar(40),
    rntrc_category varchar(40),
    rntrc_status varchar(40) NOT NULL,
    rntrc_source varchar(80) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE trucks (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    plate varchar(12) NOT NULL,
    renavam_last4 varchar(4),
    state varchar(2) NOT NULL,
    axle_count integer NOT NULL,
    vehicle_type varchar(40) NOT NULL,
    fuel_type varchar(40) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_trucks_driver_plate UNIQUE (driver_id, plate)
);

CREATE TABLE trailers (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    truck_id varchar(64) REFERENCES trucks(id),
    plate varchar(12),
    renavam_last4 varchar(4),
    trailer_type varchar(40) NOT NULL,
    axle_count integer,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE tax_profiles (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    regime varchar(60) NOT NULL,
    planning_year integer NOT NULL,
    annual_gross_limit numeric(14, 2),
    currency varchar(3) NOT NULL,
    source varchar(160),
    source_type varchar(80),
    reviewed_at timestamp with time zone,
    freshness_status varchar(40) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_tax_profiles_driver_year UNIQUE (driver_id, planning_year)
);

CREATE TABLE tax_rule_years (
    id varchar(64) PRIMARY KEY,
    regime varchar(60) NOT NULL,
    planning_year integer NOT NULL,
    annual_gross_limit numeric(14, 2),
    currency varchar(3) NOT NULL,
    formula_metadata text,
    effective_from date NOT NULL,
    effective_to date,
    source_url varchar(500),
    reviewed_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_tax_rule_years_regime_year UNIQUE (regime, planning_year)
);

CREATE TABLE ipva_rules (
    id varchar(64) PRIMARY KEY,
    state varchar(2) NOT NULL,
    vehicle_type varchar(40) NOT NULL,
    effective_year integer NOT NULL,
    rate_percent numeric(7, 4),
    currency varchar(3) NOT NULL,
    source_url varchar(500),
    reviewed_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_ipva_rules_state_vehicle_year UNIQUE (state, vehicle_type, effective_year)
);

CREATE TABLE compliance_profiles (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL UNIQUE REFERENCES drivers(id),
    rntrc_number varchar(40),
    rntrc_category varchar(40),
    rntrc_status varchar(40) NOT NULL,
    rntrc_source varchar(80) NOT NULL,
    rntrc_last_checked_at timestamp with time zone,
    official_action_url varchar(500),
    advisory_text varchar(500) NOT NULL,
    ciot_required varchar(40) NOT NULL,
    latest_ciot_status varchar(40) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE insurance_policies (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    truck_id varchar(64) REFERENCES trucks(id),
    policy_type varchar(60) NOT NULL,
    insurer varchar(160) NOT NULL,
    broker_contact varchar(160),
    policy_number_last4 varchar(4),
    starts_on date,
    expires_on date,
    annual_premium numeric(14, 2),
    monthly_reserve numeric(14, 2),
    currency varchar(3) NOT NULL,
    linked_rntrc boolean NOT NULL DEFAULT false,
    pgr_required boolean NOT NULL DEFAULT false,
    verification_status varchar(40) NOT NULL,
    document_reference varchar(160),
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);
