# Kotlin Agent Router

Use this router for Kotlin projects.

Supported modes:
- Android-only
- Kotlin Multiplatform / Compose Multiplatform

Choose exactly:
- one platform guide
- one stage guide

## Platform Selection

Use `agent-platform-android.md` when:
- the task targets an Android-only project
- target code is not in KMP source sets
- Android-specific concerns matter: manifest, resources, Activity/Fragment, WorkManager, Android tests

Use `agent-platform-kmp-compose.md` when:
- the project uses Kotlin Multiplatform
- the task touches `commonMain` or other KMP source sets
- `expect/actual` is involved
- shared contracts or shared UI are part of the task
- Compose-specific and engine-independent boundaries matter

If the repository is mixed:
- choose by the target modules of the task
- prefer KMP mode when shared KMP modules are involved

## Stage Selection

Use `agent-existing.md` when:
- the repository already exists
- architecture, build logic, and structure already exist
- the task should fit the current project shape

Use `agent-greenfield.md` when:
- the project or its foundation is being created from scratch
- the task defines the bootstrap structure
- the first feature should become the template for later work

## Combination Rule

Always combine:
- one platform guide
- one stage guide

Priority:
- existing-project rules override generic templates
- greenfield rules define bootstrap invariants
- platform rules win on platform-specific questions

## Always-On Rules

- Prefer clarity and predictable structure over personal preference.
- Ask only when uncertainty materially changes the implementation.
- If uncertainty is minor, make the safest assumption and state it briefly.
- Prefer interfaces for externally consumed contracts.
- Do not add interfaces for simple models, sealed UI contracts, or obvious internal helpers.
- Default to `internal`.
- Expose `public` only for real cross-module contracts.
- Keep dependencies directional and understandable.
- Do not add layers or modules only for appearance.
- Keep UI, navigation, DI, and business logic separated.

## Minimal Workflow

1. Select platform mode.
2. Select stage mode.
3. Read the matching platform guide.
4. Read the matching stage guide.
5. Execute without breaking the always-on rules.
