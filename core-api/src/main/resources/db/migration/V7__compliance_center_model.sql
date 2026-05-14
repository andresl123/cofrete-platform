CREATE TABLE compliance_documents (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    document_type varchar(60) NOT NULL,
    owner_type varchar(40) NOT NULL,
    owner_id varchar(64),
    title varchar(160) NOT NULL,
    identifier_last4 varchar(4),
    issued_on date,
    expires_on date,
    source varchar(80) NOT NULL,
    status varchar(40) NOT NULL,
    storage_object_key varchar(300),
    storage_content_type varchar(120),
    notes varchar(500),
    active boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE compliance_alerts (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    alert_type varchar(60) NOT NULL,
    severity varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    subject_type varchar(40) NOT NULL,
    subject_id varchar(64),
    due_on date,
    message varchar(300) NOT NULL,
    advisory_text varchar(700) NOT NULL,
    official_action_url varchar(500),
    generated_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE compliance_score_snapshots (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    score integer NOT NULL,
    status varchar(40) NOT NULL,
    component_summary text NOT NULL,
    caveats text NOT NULL,
    snapshot_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE compliance_calendar_items (
    id varchar(64) PRIMARY KEY,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    subject_type varchar(40) NOT NULL,
    subject_id varchar(64),
    title varchar(200) NOT NULL,
    due_on date NOT NULL,
    status varchar(40) NOT NULL,
    severity varchar(40) NOT NULL,
    advisory_text varchar(700) NOT NULL,
    official_action_url varchar(500),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE INDEX idx_compliance_documents_driver ON compliance_documents(driver_id);
CREATE INDEX idx_compliance_alerts_driver ON compliance_alerts(driver_id);
CREATE INDEX idx_compliance_score_snapshots_driver ON compliance_score_snapshots(driver_id);
CREATE INDEX idx_compliance_calendar_driver_due ON compliance_calendar_items(driver_id, due_on);
