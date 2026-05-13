# Documentation

Use this directory for product requirements, architecture decisions, compliance notes, API contracts, and operational runbooks.

Keep documents focused and link related service-specific details from the relevant service README when needed.

## Source Map

- `product/`: product promise, MVP scope, personas, glossary, and roadmap.
- `architecture/`: system overview, service boundaries, data model, contracts, external data retrieval, auth, security, observability, and deployment.
- `architecture/kubernetes.md`: Kubernetes production deployment conventions and cluster discovery status.
- `architecture/external-data-retrieval.md`: source retrieval strategy, freshness rules, and Linear issue ownership for external data.
- `architecture/repository-structure.md`: current and planned repository layout.
- `compliance/`: Brazil-specific advisory compliance notes, including RNTRC/ANTT, CIOT/freight floor, tolls, diesel, insurance, IPVA/licensing, tax profiles, waiting time, and official-source register.
- `agent-harness/`: agent workflow, validation, testing policy, risk register, and engineering principles.
- `runbooks/`: local development, demo seed, importer failure, incident response, and production release.

## Entry Points

These are derived entry points for faster orientation, not canonical references:

- `application-explainer.html`: product/application overview for readers who want the "what is this?" path.
- `architecture/architecture-explainer.html`: architecture overview for readers who want the "how is this shaped?" path.

## Explainer Pages

`application-explainer.html` and `architecture/architecture-explainer.html` are manually maintained derived summaries. Markdown docs remain canonical; update the relevant markdown first, then manually realign the explainer pages until a generator exists.
