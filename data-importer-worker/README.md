# Data Importer Worker

Java 21 Spring Boot background worker for data ingestion and import processing in Cofrete Platform.

The scaffold owns scheduler structure, import job stubs, RabbitMQ publisher stubs, service health, and fixture rules for external-source imports.

External-source retrieval ownership, freshness behavior, and Linear issue mapping are documented in `../docs/architecture/external-data-retrieval.md`.

## Local Development

```sh
mvn -q validate test
```

The health endpoint is exposed at:

```text
GET /actuator/health
```

## Local Infrastructure

Use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |
| MinIO | `http://minio:9000`, bucket `cofrete-local` | `http://localhost:9000`, bucket `cofrete-local` |

The matching placeholder variables are `COFRETE_DATA_IMPORTER_DATABASE_URL`, `COFRETE_DATA_IMPORTER_AMQP_URL`, `COFRETE_DATA_IMPORTER_OBJECT_STORAGE_ENDPOINT`, and `COFRETE_DATA_IMPORTER_OBJECT_STORAGE_BUCKET` in the root `.env.example`.

## Import Stubs

- `AnpDieselPriceImportJob` loads the synthetic ANP diesel fixture and can publish `fuel-price.import.completed`.
- `TollDataImportJob` publishes a no-data open-data stub for `toll-data.import.completed`.
- `ImportScheduler` is wired but disabled by default with `COFRETE_DATA_IMPORTER_SCHEDULER_ENABLED=false`.

The bundled fixture is `src/main/resources/fixtures/anp-diesel-prices-synthetic.csv`. It is synthetic test data, not official ANP data. The fixture carries freshness and confidence fields so later import behavior has the required metadata shape from the first scaffold.

## Event Contracts

Published event stubs must match `../docs/architecture/event-contracts.md` exactly:

- `fuel-price.import.completed`
- `toll-data.import.completed`

The scaffold intentionally does not add production paid toll provider integration. Toll import work is limited to the open-data event boundary until a dedicated issue defines source terms, normalization, and compliance wording.
