# Workflow

1. Start from `AGENTS.md`.
2. Read relevant source-of-truth docs.
3. Use a dedicated Git worktree branch for the task.
4. Keep the change scoped to the Linear/GitHub issue.
5. Update tests or validation for behavior changes.
6. Update docs when durable knowledge changes.
7. Add "create/update task-specific HTML explainer" to the explicit task plan before editing.
8. Create or update the task-specific HTML explainer before final validation and before handoff. The explainer should include the task ID from the branch, summarize what changed, why it changed, affected files or areas, validation run, and the next task it unblocks when relevant.
9. Treat the explainer as required handoff validation alongside service checks, `docker compose config`, `python scripts/agent_harness_check.py`, and `git diff --check`.
10. Run `python scripts/agent_harness_check.py`.
11. Run service-specific validation when a service exists and is changed.
12. Run `git diff --check`.
13. Stop with local changes for user review. Do not commit, push, open a PR, or update an existing PR until the user has reviewed the changes and confirmed validation is complete.

## Handoff

PR descriptions should include:

- what changed.
- why it changed.
- user/developer impact.
- finance/compliance caveats when relevant.
- validation commands run.
- link to the task-specific HTML explainer.
