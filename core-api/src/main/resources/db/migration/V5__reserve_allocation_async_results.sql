ALTER TABLE reserve_allocations ADD COLUMN result_event_id varchar(160);
ALTER TABLE reserve_allocations ADD COLUMN allocation_trace_id varchar(80);
ALTER TABLE reserve_allocations ADD COLUMN allocated_at timestamp with time zone;

CREATE INDEX idx_reserve_allocations_account_subject_revision
    ON reserve_allocations(account_id, allocation_subject_id, allocation_revision);
