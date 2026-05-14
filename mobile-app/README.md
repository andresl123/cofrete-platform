# Cofrete Mobile App

Expo mobile client for Cofrete Platform.

## Local Setup

```sh
npm install
cp .env.example .env.local
npm run start
```

`EXPO_PUBLIC_CORE_API_BASE_URL` points the app at Core API. The local default is `http://localhost:8080`.
Expo exposes `EXPO_PUBLIC_*` values in the client bundle, so do not put tokens, passwords, signing keys, or service credentials there.

## Validation

```sh
npm run typecheck
npm run lint
npm run test:ci
```

## MVP Routes

Navigation uses React Navigation bottom tabs. Route IDs live in `src/navigation/routes.ts`,
and placeholder content lives in `src/screens/screenContent.ts`.

- Dashboard financeiro: financial health, safe personal withdrawal, reserves, and compliance summaries.
- Motorista e caminhao: driver, truck, tax profile, and app identity entry point.
- Novo frete: freight/trip profitability decision flow.
- Carteira de reservas: reserve buckets, virtual ledger, and advisory safe personal withdrawal view.
- Centro de conformidade: RNTRC, insurance, documents, and advisory risk wording.

Mobile screens consume Core API contracts documented in `../docs/architecture/api-contracts.md`. Cofrete is not an official government, tax, accounting, legal, or insurance channel.
