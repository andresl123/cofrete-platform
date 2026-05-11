# Workflow

1. Start from `AGENTS.md`.
2. Read relevant source-of-truth docs.
3. Use a dedicated Git worktree branch for the task.
4. Keep the change scoped to the Linear/GitHub issue.
5. Update tests or validation for behavior changes.
6. Update docs when durable knowledge changes.
7. Run `python scripts/agent_harness_check.py`.
8. Run service-specific validation when a service exists and is changed.
9. Commit, push, and open a draft PR unless instructed otherwise.

## Handoff

PR descriptions should include:

- what changed.
- why it changed.
- user/developer impact.
- finance/compliance caveats when relevant.
- validation commands run.
