# Mobile App Agent Guide

Owns the mobile client application. Keep mobile source, tests, assets, and platform-specific configuration in this directory unless a task explicitly introduces shared contracts or documentation.

## Validation

Run these commands before handing off mobile changes:

```sh
npm run typecheck
npm run lint
npm run test:ci
```

Run the root harness check after repository-structure changes:

```sh
python ../scripts/agent_harness_check.py
```
