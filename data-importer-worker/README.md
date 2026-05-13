# Data Importer Worker

Background worker for data ingestion and import processing in Cofrete Platform.

Implementation details, local setup, and import contracts will be added with worker scaffolding tasks.

Until the worker is scaffolded, external-source retrieval ownership, freshness behavior, and Linear issue mapping are documented in `../docs/architecture/external-data-retrieval.md`.

## Local Infrastructure Placeholders

Until the worker is scaffolded, use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |
| MinIO | `http://minio:9000`, bucket `cofrete-local` | `http://localhost:9000`, bucket `cofrete-local` |

The matching placeholder variables are `COFRETE_DATA_IMPORTER_DATABASE_URL`, `COFRETE_DATA_IMPORTER_AMQP_URL`, `COFRETE_DATA_IMPORTER_OBJECT_STORAGE_ENDPOINT`, and `COFRETE_DATA_IMPORTER_OBJECT_STORAGE_BUCKET` in the root `.env.example`.
