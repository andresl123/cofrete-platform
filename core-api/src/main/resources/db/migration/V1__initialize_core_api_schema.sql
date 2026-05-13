CREATE TABLE IF NOT EXISTS schema_marker (
    id smallint PRIMARY KEY,
    description varchar(120) NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT now()
);
