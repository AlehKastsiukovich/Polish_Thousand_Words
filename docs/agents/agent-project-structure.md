# Project Structure Guide

Use this guide when defining or extending the project module layout.

## Top-Level Shape

The project should use three clear areas:
- app or entry modules
- `core/*` modules
- `feature/*` modules

## Entry Modules

Entry modules are responsible for:
- app startup
- root DI composition
- root navigation composition
- platform wiring that belongs at the app boundary

Entry modules must not own feature business logic.

## Core Modules

Use `core/*` for reusable modules shared across features or across the app.

Typical `core/*` responsibilities:
- domain-independent shared utilities
- common UI primitives when they are truly reusable
- navigation contracts and navigation infrastructure
- data access abstractions
- storage or database infrastructure
- cross-feature service contracts
- base state-management contracts

Rules:
- `core/*` may be depended on by entry and feature modules
- `core/*` must not depend on `feature/*`
- keep `core/*` focused on reusable or foundational responsibilities
- do not move feature-specific logic into `core/*` just to "share" it early

## Feature Modules

Use `feature/*` for vertical feature slices.

Each feature module should own as much of its feature as possible:
- UI
- screen state holder, presenter, or view model
- feature DI
- navigation integration
- feature-local models and contracts
- feature-specific use cases or orchestration

Rules:
- features may depend on `core/*`
- features should not know app-wide details unless exposed through an explicit contract
- avoid direct cross-feature dependencies when a contract can express the boundary better

## Dependency Direction

Default direction:
- entry -> feature
- entry -> core
- feature -> core

Avoid:
- core -> feature
- feature -> entry
- broad cross-feature imports without a clear contract

## Naming

Prefer one consistent style across folders and Gradle paths.

Recommended:
- `core/navigation-api`
- `core/data`
- `feature/home`
- `feature/onboarding`

Do not mix naming styles without a reason.

## Practical Rule

When adding a new module, decide first:
- is this reusable across multiple features or app layers
- or is it part of one vertical feature slice

If it is reusable and foundational, place it under `core/*`.
If it belongs to one user-facing capability, place it under `feature/*`.
