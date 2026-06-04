# Greenfield Guide

Use this guide when defining a new project foundation.

Main principle:
- keep the bootstrap small
- define invariants early
- avoid a structure that must be broken later

Platform-specific shape comes from the selected platform guide.

## Required Invariants

### Dependency Direction

- root or entry modules may depend on core, shared, and feature modules
- feature modules must not depend on app-wide details without an explicit contract
- more general modules must not depend on more concrete ones
- cross-feature imports should go through explicit contracts

### Entry Ownership

The root entry module must:
- be the single app entry point
- assemble root DI
- assemble root navigation
- avoid feature business logic

### Feature Ownership

Each feature should be a vertical slice with clear ownership of:
- UI
- screen state holder or presenter
- feature DI
- navigation integration
- feature-specific models or contracts that are not shared

### State Management

- keep one consistent screen state-management style
- do not introduce competing patterns without a clear reason
- keep shared base contracts in one obvious place

### Visibility

- default to `internal`
- expose `public` only for real cross-module contracts
- avoid unnecessary `public` concrete classes

### First Feature as Template

The first feature should demonstrate:
- naming conventions
- package and file layout
- DI wiring
- navigation integration
- state, event, and effect flow
- visibility discipline
- where interfaces are needed and where they are not

## Allowed Starting Shapes

Greenfield does not require maximum modularization on day one.

Allowed shapes:
- minimal bootstrap
- modular bootstrap

Use minimal bootstrap for an MVP or fast prototype when the invariants still hold.

Use modular bootstrap when you expect:
- multiple features
- non-trivial module contracts
- team growth
- storage, network, or platform integrations
- stronger isolation and testability requirements

## Bootstrap Order

1. Configure Gradle, version catalog, and convention plugins.
2. Choose the package root and platform-specific structure.
3. Create the entry module.
4. Create the minimum shared or core foundation the platform shape needs.
5. Create the first feature as a vertical slice.
6. Wire root DI.
7. Wire root navigation.
8. Add data, storage, or network modules only when they are actually needed.

## Defaults

If the user did not specify architecture choices:
- choose the smallest sufficient foundation
- do not add modules or layers without a reason
- do not violate the required invariants
- take platform-specific defaults from the selected platform guide

## Final Check

Before finishing bootstrap, verify:
- there is one root module
- there is at least one feature
- root DI starts from one obvious place
- navigation has one clear entry point
- package naming is consistent
- state management is consistent
- dependencies point in the right direction
- there are no unnecessary `public` concrete classes
- the first feature can serve as the template for later features
