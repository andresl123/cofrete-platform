# Security

## Sensitive Data

Cofrete may handle CPF/CNPJ metadata, truck plate/RENAVAM, RNTRC number, insurance documents, receipts, freight values, customer payment history, document images, and future location data.

## Requirements

- Never commit real secrets.
- Use environment variables or secret managers for credentials.
- Use least-privilege service access.
- Do not log tokens, full document content, or unnecessary sensitive identifiers.
- Protect document downloads through authenticated APIs or signed URLs.
- Add audit logging for financial, document, and compliance changes.
- Encrypt production transport with TLS.
- Keep dev/demo data synthetic.

## Review Triggers

Security review is required when a change adds authentication, authorization, document storage, payment data, location data, import credentials, or support/admin access.
