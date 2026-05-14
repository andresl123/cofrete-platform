CREATE TABLE insurance_requirement_rules (
    id varchar(64) PRIMARY KEY,
    requirement_scope varchar(120) NOT NULL,
    policy_type varchar(60) NOT NULL,
    required boolean NOT NULL DEFAULT true,
    effective_from date NOT NULL,
    effective_to date,
    source_name varchar(160),
    source_url varchar(500),
    reviewed_at timestamp with time zone,
    freshness_status varchar(40) NOT NULL,
    confidence varchar(40) NOT NULL,
    notes varchar(700),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_insurance_requirement_rule_scope_policy_from UNIQUE (requirement_scope, policy_type, effective_from)
);

CREATE INDEX idx_insurance_requirement_rules_effective
    ON insurance_requirement_rules(policy_type, effective_from, effective_to);
