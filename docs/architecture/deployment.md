# Deployment

Deployment details will mature after service scaffolding. Until then, this document records target assumptions.

## Target Environments

- Local: Docker Compose for PostgreSQL, RabbitMQ, MinIO, and service networking.
- CI: GitHub Actions for harness and service checks.
- Production: containerized services with managed database, object storage, messaging, and secret management.

## Release Rules

- Run repository harness checks before every PR.
- Run service-specific tests for changed services.
- Review compliance wording for changes touching RNTRC, CIOT, Vale-Pedagio, ANP, insurance, MEI, taxes, or documents.
- Verify demo seed and smoke flow before MVP release.

## Config Rules

- Production secrets must not live in Git.
- Local `.env.example` must use safe placeholders only.
- Data import jobs should be feature-flagged or schedulable.
