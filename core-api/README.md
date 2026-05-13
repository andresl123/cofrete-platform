# Core API

Backend API service for Cofrete Platform.

Implementation details, local setup, and service contracts will be added with the API scaffolding tasks.

## Local Infrastructure Placeholders

Until the API is scaffolded, use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |
| MinIO | `http://minio:9000`, bucket `cofrete-local` | `http://localhost:9000`, bucket `cofrete-local` |

The matching placeholder variables are `COFRETE_CORE_API_DATABASE_URL`, `COFRETE_CORE_API_AMQP_URL`, `COFRETE_CORE_API_OBJECT_STORAGE_ENDPOINT`, and `COFRETE_CORE_API_OBJECT_STORAGE_BUCKET` in the root `.env.example`.
