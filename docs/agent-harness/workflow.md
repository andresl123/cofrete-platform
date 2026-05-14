# Workflow

1. Start from `AGENTS.md`.
2. Read relevant source-of-truth docs.
3. Use a dedicated Git worktree branch for the task.
4. Keep the change scoped to the Linear/GitHub issue.
5. Update tests or validation for behavior changes.
6. Update docs when durable knowledge changes.
7. Add "create/update task-specific HTML explainer under `docs/explainers/`" to the explicit task plan before editing.
8. Create or update the task-specific HTML explainer before final validation and before handoff. The explainer should include the task ID from the branch, summarize what changed, why it changed, affected files or areas, validation run, and the next task it unblocks when relevant. Explainers are derived handoff aids, not source-of-truth docs.
9. If the task changes frontend or mobile files, run the Impeccable review loop from `frontend-review-loop.md` after implementation and initial frontend/mobile validation, before `grill-with-docs` and before final validation. Stop early when no meaningful frontend/mobile issue remains, or after 15 total Impeccable passes.
10. For Linear issue implementations, run the `grill-with-docs` review-and-fix loop from `linear-review-loop.md` after the implementation is complete and before final validation. Stop early when no meaningful issue remains, or after 10 total loops.
11. Treat the explainer, triggered Impeccable loop summary, and `grill-with-docs` loop summary as required handoff validation alongside service checks, `docker compose config`, `python scripts/agent_harness_check.py`, and `git diff --check`.
12. Run `python scripts/agent_harness_check.py`.
13. Run service-specific validation when a service exists and is changed.
14. Run `git diff --check`.
15. Stop with local changes for user review. Do not commit, push, open a PR, or update an existing PR until the user has reviewed the changes and confirmed validation is complete.

## Handoff

PR descriptions should include:

- what changed.
- why it changed.
- user/developer impact.
- finance/compliance caveats when relevant.
- validation commands run.
- Impeccable loop trigger status, commands run, fixes applied, and remaining UI risks when frontend or mobile files changed.
- `grill-with-docs` loop count, issues found, fixes applied, and remaining risks or assumptions for Linear issues.
- link to the task-specific HTML explainer.
