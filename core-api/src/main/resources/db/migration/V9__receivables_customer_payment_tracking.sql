CREATE TABLE customers (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    name varchar(160) NOT NULL,
    tax_id_type varchar(20),
    tax_id_last4 varchar(4),
    contact_name varchar(120),
    contact_phone varchar(40),
    payment_terms_days integer NOT NULL,
    notes varchar(500),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_customers_account_name UNIQUE (account_id, name)
);

CREATE TABLE receivables (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    customer_id varchar(64) NOT NULL REFERENCES customers(id),
    trip_id varchar(64),
    type varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    amount numeric(14, 2) NOT NULL,
    paid_amount numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    due_date date NOT NULL,
    payment_method varchar(40) NOT NULL,
    note varchar(500),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE receivable_payments (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    receivable_id varchar(64) NOT NULL REFERENCES receivables(id),
    amount numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    paid_date date NOT NULL,
    payment_method varchar(40) NOT NULL,
    note varchar(500),
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE receivable_status_changes (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL REFERENCES app_users(account_id),
    receivable_id varchar(64) NOT NULL REFERENCES receivables(id),
    previous_status varchar(40),
    new_status varchar(40) NOT NULL,
    reason varchar(80) NOT NULL,
    changed_at timestamp with time zone NOT NULL,
    note varchar(500)
);

CREATE INDEX idx_customers_account_created ON customers(account_id, created_at);
CREATE INDEX idx_receivables_account_status_due ON receivables(account_id, status, due_date);
CREATE INDEX idx_receivables_customer_due ON receivables(customer_id, due_date);
CREATE INDEX idx_receivable_payments_receivable_created ON receivable_payments(receivable_id, created_at);
CREATE INDEX idx_receivable_status_changes_receivable_changed ON receivable_status_changes(receivable_id, changed_at);
