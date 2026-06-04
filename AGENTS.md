# AGENTS

## Git Workflow

- Use GitHub as the remote git provider.
- Create task branches from `main` using the format `feature/PT-<n>-<short-task-name>`.
- Keep exactly one commit per task branch.
- Update that single commit with `git commit --amend` for all further changes in the same task.
- Use the commit message format `[PT-<n>] <short-task-name>`.
- Open pull requests from `feature/PT-<n>-<short-task-name>` to `main`.
- If an amended commit was already pushed, update the remote branch with `git push --force-with-lease`.
