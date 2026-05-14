# Frontend Review Loop

Tasks that change frontend or mobile files must run an end-of-task Impeccable review loop before final handoff.

## Trigger

Run this loop only when the task changes user-facing frontend or mobile files, including:

- `mobile-app/` screens, components, navigation, styles, assets, tests, or app configuration.
- `web-app/` screens, components, routes, styles, assets, tests, or app configuration.
- shared frontend design-system files such as `PRODUCT.md`, `DESIGN.md`, or `.impeccable/design.json` when changed as part of UI work.

Do not run the loop for backend-only, worker-only, infrastructure-only, or documentation-only tasks unless the task also changes frontend or mobile files.

## Timing

Run the loop at the end of the issue task, after the initial implementation is complete and after the relevant frontend/mobile tests have passed at least once.

Do not run the full loop before implementation starts. Before editing UI files, use Impeccable only for its required context loading, register selection, and any task-specific design preflight.

For Linear issues, run this loop before the `grill-with-docs` review loop and before the final validation pass so any fixes are covered by the final checks.

## Loop Contract

Run up to 15 Impeccable passes.

For each pass:

1. Run the next recommended Impeccable command for the current frontend/mobile state.
2. Review the command output against the changed UI, `PRODUCT.md`, `DESIGN.md`, `.impeccable/design.json`, and relevant service docs.
3. Apply concrete fixes directly when the command finds real issues.
4. Update tests, design docs, or the task explainer when the fixes change behavior, visual-system rules, or handoff context.
5. Choose the next most useful Impeccable command from the prior pass findings.
6. Stop early when the remaining commands would be repetitive or no meaningful frontend/mobile issue remains.

Common command order is:

```text
polish -> adapt -> audit -> harden -> clarify -> layout -> typeset -> audit -> polish
```

This order is guidance, not a requirement. Choose the next command based on actual findings.

## Required Handoff Summary

The final handoff for any triggered loop must include:

- whether the loop was triggered and why;
- number of Impeccable passes executed;
- commands run in order;
- issues found;
- fixes applied;
- frontend/mobile tests and checks run after the loop;
- remaining UI risks or assumptions.

If the loop was not triggered, state that no frontend or mobile files changed.
