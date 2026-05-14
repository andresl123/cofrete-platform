# Mobile App Agent Guide

Owns the mobile client application. Keep mobile source, tests, assets, and platform-specific configuration in this directory unless a task explicitly introduces shared contracts or documentation.

## Validation

Run these commands before handing off mobile changes:

```sh
npm run typecheck
npm run lint
npm run test:ci
```

After mobile UI changes, run the end-of-task Impeccable review loop from `../docs/agent-harness/frontend-review-loop.md`. Run it after the implementation and initial mobile validation pass, not before implementation. Include the loop count, commands run, fixes applied, and remaining UI risks in handoff.

Run the root harness check after repository-structure changes:

```sh
python ../scripts/agent_harness_check.py
```
