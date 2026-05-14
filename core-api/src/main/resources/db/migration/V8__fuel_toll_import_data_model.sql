CREATE TABLE import_jobs (
    id varchar(64) PRIMARY KEY,
    source varchar(80) NOT NULL,
    dataset varchar(120) NOT NULL,
    status varchar(40) NOT NULL,
    source_url varchar(500),
    source_period_start date,
    source_period_end date,
    retrieved_at timestamp with time zone NOT NULL,
    completed_at timestamp with time zone,
    row_count integer NOT NULL,
    freshness_status varchar(40) NOT NULL,
    confidence varchar(80) NOT NULL,
    file_hash varchar(160),
    parser_error_summary varchar(1000),
    correlation_id varchar(160),
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE fuel_prices (
    id varchar(64) PRIMARY KEY,
    fuel_type varchar(40) NOT NULL,
    state varchar(2) NOT NULL,
    city varchar(120),
    price_per_liter numeric(10, 4) NOT NULL,
    currency varchar(3) NOT NULL,
    source varchar(80) NOT NULL,
    source_type varchar(80) NOT NULL,
    source_url varchar(500),
    source_period_start date NOT NULL,
    source_period_end date NOT NULL,
    retrieved_at timestamp with time zone NOT NULL,
    freshness_status varchar(40) NOT NULL,
    confidence varchar(80) NOT NULL,
    import_audit_id varchar(64) NOT NULL REFERENCES import_jobs(id),
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE driver_fuel_reports (
    id varchar(64) PRIMARY KEY,
    account_id varchar(64) NOT NULL,
    driver_id varchar(64) NOT NULL REFERENCES drivers(id),
    fuel_type varchar(40) NOT NULL,
    state varchar(2) NOT NULL,
    city varchar(120),
    station_name varchar(160),
    price_per_liter numeric(10, 4) NOT NULL,
    currency varchar(3) NOT NULL,
    source varchar(80) NOT NULL,
    source_type varchar(80) NOT NULL,
    confidence varchar(80) NOT NULL,
    receipt_reference varchar(160),
    reported_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE truck_consumption_profiles (
    truck_id varchar(64) PRIMARY KEY REFERENCES trucks(id),
    loaded_avg_km_l numeric(8, 4) NOT NULL,
    empty_avg_km_l numeric(8, 4) NOT NULL,
    last_30_days_avg_km_l numeric(8, 4) NOT NULL,
    confidence varchar(80) NOT NULL,
    source_window_start date,
    source_window_end date,
    calculated_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE toll_plazas (
    id varchar(64) PRIMARY KEY,
    external_id varchar(120) NOT NULL UNIQUE,
    name varchar(160) NOT NULL,
    highway varchar(40) NOT NULL,
    state varchar(2) NOT NULL,
    municipality varchar(120),
    km_marker numeric(8, 2),
    direction varchar(40),
    latitude numeric(10, 6),
    longitude numeric(10, 6),
    source varchar(80) NOT NULL,
    source_type varchar(80) NOT NULL,
    source_url varchar(500),
    freshness_status varchar(40) NOT NULL,
    confidence varchar(80) NOT NULL,
    import_audit_id varchar(64) NOT NULL REFERENCES import_jobs(id),
    created_at timestamp with time zone NOT NULL
);

CREATE TABLE toll_tariffs (
    id varchar(64) PRIMARY KEY,
    toll_plaza_id varchar(64) NOT NULL REFERENCES toll_plazas(id),
    vehicle_category varchar(40) NOT NULL,
    axle_count integer NOT NULL,
    amount numeric(14, 2) NOT NULL,
    currency varchar(3) NOT NULL,
    effective_start date NOT NULL,
    effective_end date,
    source varchar(80) NOT NULL,
    freshness_status varchar(40) NOT NULL,
    confidence varchar(80) NOT NULL,
    import_audit_id varchar(64) NOT NULL REFERENCES import_jobs(id),
    created_at timestamp with time zone NOT NULL
);

CREATE INDEX idx_fuel_prices_lookup ON fuel_prices(fuel_type, state, city, source_period_end, retrieved_at);
CREATE INDEX idx_driver_fuel_reports_driver_created ON driver_fuel_reports(driver_id, created_at);
CREATE INDEX idx_toll_plazas_state ON toll_plazas(state);
CREATE INDEX idx_toll_tariffs_plaza_axle ON toll_tariffs(toll_plaza_id, axle_count, effective_start);
CREATE INDEX idx_import_jobs_dataset_source ON import_jobs(dataset, source, completed_at);

INSERT INTO import_jobs (
    id, source, dataset, status, source_url, source_period_start, source_period_end,
    retrieved_at, completed_at, row_count, freshness_status, confidence, file_hash,
    parser_error_summary, correlation_id, created_at
) VALUES
(
    'import_anp_synthetic_20260509', 'ANP', 'diesel_prices', 'COMPLETED',
    'https://www.gov.br/anp/pt-br/assuntos/precos-e-defesa-da-concorrencia/precos/levantamento-de-precos-de-combustiveis-ultimas-semanas-pesquisadas',
    DATE '2026-05-03', DATE '2026-05-09',
    TIMESTAMP WITH TIME ZONE '2026-05-11T11:59:00Z',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:20Z',
    3, 'CURRENT', 'SYNTHETIC_FIXTURE', 'synthetic-fixture',
    NULL, 'seed-anp-diesel', TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:20Z'
),
(
    'import_antt_synthetic_20260531', 'ANTT_DADOS_ABERTOS', 'toll_plazas_and_tariffs', 'COMPLETED',
    'https://dados.antt.gov.br/dataset/praca-de-pedagio',
    DATE '2026-05-01', DATE '2026-05-31',
    TIMESTAMP WITH TIME ZONE '2026-05-11T11:58:00Z',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z',
    2, 'CURRENT', 'SYNTHETIC_FIXTURE', 'synthetic-fixture',
    NULL, 'seed-antt-toll', TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z'
);

INSERT INTO fuel_prices (
    id, fuel_type, state, city, price_per_liter, currency, source, source_type, source_url,
    source_period_start, source_period_end, retrieved_at, freshness_status, confidence,
    import_audit_id, created_at
) VALUES
(
    'fuel_price_go_goiania_s10_20260509', 'DIESEL_S10', 'GO', 'GOIANIA', 6.1800, 'BRL',
    'ANP', 'official_dataset',
    'https://www.gov.br/anp/pt-br/assuntos/precos-e-defesa-da-concorrencia/precos/levantamento-de-precos-de-combustiveis-ultimas-semanas-pesquisadas',
    DATE '2026-05-03', DATE '2026-05-09',
    TIMESTAMP WITH TIME ZONE '2026-05-11T11:59:00Z',
    'CURRENT', 'SYNTHETIC_FIXTURE', 'import_anp_synthetic_20260509',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:20Z'
),
(
    'fuel_price_sp_saopaulo_s10_20260509', 'DIESEL_S10', 'SP', 'SAO PAULO', 6.1000, 'BRL',
    'ANP', 'official_dataset',
    'https://www.gov.br/anp/pt-br/assuntos/precos-e-defesa-da-concorrencia/precos/levantamento-de-precos-de-combustiveis-ultimas-semanas-pesquisadas',
    DATE '2026-05-03', DATE '2026-05-09',
    TIMESTAMP WITH TIME ZONE '2026-05-11T11:59:00Z',
    'CURRENT', 'SYNTHETIC_FIXTURE', 'import_anp_synthetic_20260509',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:20Z'
),
(
    'fuel_price_pr_curitiba_s500_20260509', 'DIESEL_S500', 'PR', 'CURITIBA', 5.9800, 'BRL',
    'ANP', 'official_dataset',
    'https://www.gov.br/anp/pt-br/assuntos/precos-e-defesa-da-concorrencia/precos/levantamento-de-precos-de-combustiveis-ultimas-semanas-pesquisadas',
    DATE '2026-05-03', DATE '2026-05-09',
    TIMESTAMP WITH TIME ZONE '2026-05-11T11:59:00Z',
    'CURRENT', 'SYNTHETIC_FIXTURE', 'import_anp_synthetic_20260509',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:20Z'
);

INSERT INTO toll_plazas (
    id, external_id, name, highway, state, municipality, km_marker, direction, latitude,
    longitude, source, source_type, source_url, freshness_status, confidence, import_audit_id, created_at
) VALUES
(
    'toll_plaza_go_060_001', 'ANTT-GO-060-001', 'Praca de Pedagio Goiania Sul',
    'BR-060', 'GO', 'GOIANIA', 12.50, 'BOTH', -16.760000, -49.300000,
    'ANTT_DADOS_ABERTOS', 'official_open_dataset', 'https://dados.antt.gov.br/dataset/praca-de-pedagio',
    'CURRENT', 'SYNTHETIC_FIXTURE', 'import_antt_synthetic_20260531',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z'
),
(
    'toll_plaza_sp_330_001', 'ANTT-SP-330-001', 'Praca de Pedagio Anhanguera Norte',
    'SP-330', 'SP', 'CAMPINAS', 104.20, 'BOTH', -22.910000, -47.060000,
    'ANTT_DADOS_ABERTOS', 'official_open_dataset', 'https://dados.antt.gov.br/dataset/praca-de-pedagio',
    'CURRENT', 'SYNTHETIC_FIXTURE', 'import_antt_synthetic_20260531',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z'
);

INSERT INTO toll_tariffs (
    id, toll_plaza_id, vehicle_category, axle_count, amount, currency, effective_start,
    effective_end, source, freshness_status, confidence, import_audit_id, created_at
) VALUES
(
    'toll_tariff_go_060_001_truck_6_202605', 'toll_plaza_go_060_001', 'TRUCK', 6, 42.50, 'BRL',
    DATE '2026-05-01', DATE '2026-05-31', 'ANTT_DADOS_ABERTOS', 'CURRENT',
    'SYNTHETIC_FIXTURE', 'import_antt_synthetic_20260531',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z'
),
(
    'toll_tariff_sp_330_001_truck_6_202605', 'toll_plaza_sp_330_001', 'TRUCK', 6, 95.20, 'BRL',
    DATE '2026-05-01', DATE '2026-05-31', 'ANTT_DADOS_ABERTOS', 'CURRENT',
    'SYNTHETIC_FIXTURE', 'import_antt_synthetic_20260531',
    TIMESTAMP WITH TIME ZONE '2026-05-11T12:00:30Z'
);
