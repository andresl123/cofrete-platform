# Linear Review Loop

Every implemented Linear issue must receive a `grill-with-docs` review-and-fix loop before handoff.

## When To Run

Run this loop after the initial implementation is complete and service-specific validation has passed at least once. Run it before the final validation pass and before asking the user to review local changes.

If the user names `grill-me-with-docs`, use the installed `grill-with-docs` skill unless an exact `grill-me-with-docs` skill is available in the session.

## Loop Contract

Run up to 10 total review loops.

For each loop:

1. Review the Linear issue, related code changes, tests, canonical docs, service docs, and task explainer.
2. Produce a clear result.
3. If a real issue is found, state what the issue is, why it matters, and the recommended fix.
4. Apply the fix directly in code or documentation.
5. Re-run the loop.
6. Stop early when no meaningful issue remains.

Do not continue just to reach 10 loops. If the same issue appears again, investigate why the previous fix did not fully resolve it.

## Review Priorities

- Correctness against the Linear issue goal, deliverables, and acceptance criteria.
- Consistency with canonical markdown docs, especially product, architecture, compliance, and API/event contracts.
- Terminology alignment with `CONTEXT.md`.
- Test coverage for issue-critical behavior and risk boundaries.
- Documentation alignment in service README/AGENTS files and the task explainer.
- Maintainability of implementation decisions made during the issue.

## Handoff Summary

The final handoff for a Linear issue must include:

- number of `grill-with-docs` loops executed;
- issues found;
- fixes applied;
- tests and checks run after the loop;
- remaining risks or assumptions.
