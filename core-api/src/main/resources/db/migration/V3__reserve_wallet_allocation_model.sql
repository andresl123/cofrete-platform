CREATE TABLE reserve_rules (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    bucket varchar(60) NOT NULL,
    active_bucket varchar(60),
    policy varchar(40) NOT NULL,
    rate numeric(9, 6),
    fixed_amount numeric(14, 2),
    per_km_amount numeric(14, 4),
    target_balance numeric(14, 2),
    currency varchar(3) NOT NULL,
    effective_from date,
    effective_to date,
    active boolean NOT NULL DEFAULT true,
    source_assumption varchar(240),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_reserve_rules_account_active_bucket UNIQUE (account_id, active_bucket)
);

CREATE TABLE reserve_wallets (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    bucket varchar(60) NOT NULL,
    current_balance numeric(14, 2) NOT NULL,
    target_balance numeric(14, 2),
    currency varchar(3) NOT NULL,
    last_allocation_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_reserve_wallets_account_bucket UNIQUE (account_id, bucket)
);

CREATE TABLE reserve_allocations (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    idempotency_key varchar(160) NOT NULL,
    request_fingerprint varchar(64) NOT NULL,
    trip_id varchar(64),
    freight_payment_id varchar(64),
    allocation_subject_id varchar(64),
    allocation_revision integer NOT NULL,
    gross_amount numeric(14, 2) NOT NULL,
    pass_through_amount numeric(14, 2) NOT NULL,
    allocatable_amount numeric(14, 2) NOT NULL,
    required_reserve_amount numeric(14, 2) NOT NULL,
    safe_personal_withdrawal numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    reason varchar(60) NOT NULL,
    status varchar(40) NOT NULL,
    requested_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_reserve_allocations_account_idempotency UNIQUE (account_id, idempotency_key)
);

CREATE TABLE reserve_transactions (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    wallet_id varchar(64) NOT NULL REFERENCES reserve_wallets(id),
    allocation_id varchar(64) REFERENCES reserve_allocations(id),
    bucket varchar(60) NOT NULL,
    transaction_type varchar(40) NOT NULL,
    amount numeric(14, 2) NOT NULL,
    balance_after numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    source_type varchar(60) NOT NULL,
    source_reference varchar(160) NOT NULL,
    note varchar(240),
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_reserve_transactions_allocation_bucket UNIQUE (allocation_id, bucket, transaction_type)
);

CREATE INDEX idx_reserve_transactions_account_created ON reserve_transactions(account_id, created_at);
CREATE INDEX idx_reserve_transactions_wallet_created ON reserve_transactions(wallet_id, created_at);
