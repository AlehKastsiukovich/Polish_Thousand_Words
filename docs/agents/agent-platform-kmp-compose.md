# KMP / Compose Multiplatform Guide

Use this guide for Kotlin Multiplatform and Compose Multiplatform work.

It complements the selected stage guide. It does not replace it.

## Scope

Use this guide when:
- the project uses Kotlin Multiplatform
- the task touches `commonMain` or platform source sets
- shared contracts or shared UI are involved
- `expect/actual`, shared boundaries, or Compose independence matter

## Source Set Rules

- Put shared logic in `commonMain`.
- Put platform wiring in `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`, or another platform source set.
- Split tests along the same shared vs platform boundary.
- Use `expect/actual` only for real platform dependencies or APIs.

Do not:
- leak Android, iOS, JVM, or Wasm APIs into `commonMain`
- add extra source sets before real platform logic exists
- hide platform behavior inside shared code without a clear boundary

## Compose Independence

Not every shared module should depend on Compose.

Keep Compose out of modules that mainly contain:
- domain logic
- engine-independent contracts
- navigation abstractions
- shared service contracts

If a contract contains `@Composable`, Compose UI types, or Compose-only entry points, keep it:
- in a Compose-specific module
- or in a feature-local Compose contract
- but not in a pure shared API module

## Navigation Split

If navigation is modularized, a safe split is:
- `navigation-api` for engine-independent contracts
- `navigation-entry-api` for Compose-based but engine-agnostic screen entry contracts
- `navigation-compose` for the concrete Compose navigation implementation

Rules:
- `navigation-api` must not depend on Compose
- Compose-only destination contracts must not masquerade as generic APIs
- the root nav host usually belongs in the entry layer
- feature route integration should live near the feature

If the existing project already has a coherent split, continue it.

## Shared UI

Shared Compose UI in `commonMain` is valid only when it is truly shared.

Do not:
- move platform-specific UI into `commonMain` just for symmetry
- put platform resources, permissions, or OS integrations into shared UI
- force all UI to be shared if some screens are naturally platform-specific

UI should:
- receive state and callbacks
- avoid direct platform service access when a contract can be used
- avoid mixing feature logic with platform wiring

## DI and Platform Wiring

- Keep shared contracts and shared logic in shared layers.
- Keep platform-specific providers, factories, and integrations near their platform implementations.
- Connect platform modules from root DI composition.
- If shared logic needs a platform service, define a shared contract first and bind the platform implementation separately.

## Storage, Network, and Platform Services

- Shared contracts and orchestration may live in shared modules.
- Platform clients and platform wiring belong in platform source sets or platform modules.
- Use `expect/actual` only when it simplifies the boundary instead of blurring it.
- Do not add a platform-specific dependency to `commonMain` if only one platform needs it.
