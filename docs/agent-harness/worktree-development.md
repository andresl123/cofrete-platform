# Worktree Development

Cofrete tasks should use dedicated Git worktrees so unrelated changes do not mix.

Preferred command:

```sh
scripts/start_task_worktree.sh <task-name> [base-branch]
```

Examples:

```sh
scripts/start_task_worktree.sh cof-003-local-infra main
scripts/start_task_worktree.sh cof-005-core-api origin/main
```

The helper creates sibling worktrees under:

```text
../cofrete-platform-worktrees/
```

## Rules

- Start from the intended base branch.
- Keep one Linear/GitHub issue per worktree unless the user explicitly asks otherwise.
- Run `python scripts/agent_harness_check.py` before handoff.
- Do not remove other worktrees unless the user asks.
- Do not stage unrelated files from another worktree.
