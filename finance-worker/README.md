# Finance Worker

Background worker for finance-related processing in Cofrete Platform.

Implementation details, local setup, and processing contracts will be added with worker scaffolding tasks.

## Local Infrastructure Placeholders

Until the worker is scaffolded, use the root Docker Compose stack as the local dependency contract:

| Dependency | Container connection | Host connection |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://postgres:5432/cofrete_local` | `jdbc:postgresql://localhost:5432/cofrete_local` |
| RabbitMQ | `amqp://cofrete:cofrete_local_password@rabbitmq:5672/cofrete` | `amqp://cofrete:cofrete_local_password@localhost:5672/cofrete` |

The matching placeholder variables are `COFRETE_FINANCE_WORKER_DATABASE_URL` and `COFRETE_FINANCE_WORKER_AMQP_URL` in the root `.env.example`.
