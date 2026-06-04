# AGENTS

## Git Workflow

- Use GitHub as the remote git provider.
- Create task branches from `main` using the format `feature/PT-<n>-<short-task-name>`.
- Keep exactly one commit per task branch.
- Update that single commit with `git commit --amend` for all further changes in the same task.
- Use the commit message format `[PT-<n>] <short-task-name>`.
- Open pull requests from `feature/PT-<n>-<short-task-name>` to `main`.
- If an amended commit was already pushed, update the remote branch with `git push --force-with-lease`.

## Agent Guides

- Use [docs/agents/agent-kotlin.md](/Users/alehkastsiukovich/AndroidStudioProjects/PolishThousand/docs/agents/agent-kotlin.md) as the router for Kotlin work.
- Use [docs/agents/agent-platform-kmp-compose.md](/Users/alehkastsiukovich/AndroidStudioProjects/PolishThousand/docs/agents/agent-platform-kmp-compose.md) for KMP and Compose Multiplatform constraints.
- Use [docs/agents/agent-greenfield.md](/Users/alehkastsiukovich/AndroidStudioProjects/PolishThousand/docs/agents/agent-greenfield.md) when shaping new project foundations.
- Use [docs/agents/agent-project-structure.md](/Users/alehkastsiukovich/AndroidStudioProjects/PolishThousand/docs/agents/agent-project-structure.md) for the module taxonomy and dependency direction.
