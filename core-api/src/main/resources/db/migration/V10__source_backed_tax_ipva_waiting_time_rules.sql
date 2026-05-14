ALTER TABLE ipva_rules ADD COLUMN freshness_status varchar(40) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE ipva_rules ADD COLUMN licensing_source_url varchar(500);
ALTER TABLE ipva_rules ADD COLUMN licensing_reviewed_at timestamp with time zone;
ALTER TABLE ipva_rules ADD COLUMN licensing_freshness_status varchar(40) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE tax_rule_years ADD COLUMN cargo_transport_taxable_percent numeric(7, 6);
ALTER TABLE tax_rule_years ADD COLUMN freshness_status varchar(40) NOT NULL DEFAULT 'UNKNOWN';

CREATE TABLE waiting_time_rules (
    id varchar(64) PRIMARY KEY,
    threshold_hours numeric(6, 2) NOT NULL,
    rate_per_ton_hour numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    effective_from date NOT NULL,
    effective_to date,
    source_url varchar(500),
    reviewed_at timestamp with time zone,
    freshness_status varchar(40) NOT NULL,
    confidence varchar(40) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_waiting_time_rules_effective_from UNIQUE (effective_from)
);
