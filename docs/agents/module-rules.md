# Modularization & Dependency Rules

## Module Structure
*   **Feature Split:** Split features into `:api` and `:impl`.
    *   `:api`: Contains navigation routes, interfaces, and minimal public models.
    *   `:impl`: Contains UI, ViewModels, and Hilt modules.
*   **Core Modules:** `core:*` modules must never depend on `feature:*` or `app`.

## Dependency Flow
*   `feature:*:impl` -> `feature:*:api`
*   `feature:*:impl` -> `core:*`
*   `feature:*:impl` -> `feature:other:api` (NEVER depend on another `:impl`).
*   `app` -> all `feature:*:api` and `feature:*:impl`.

## Build Logic & Conventions
*   **Convention Plugins:** Every module must use `soft.divan.*` convention plugins from `build-logic`.
    *   `soft.divan.android.library` for core/feature modules.
    *   `soft.divan.feature.api` for API modules.
    *   `soft.divan.feature.impl` for Implementation modules.
*   **Module README:** Every module MUST contain a `README.md` describing its purpose and dependencies.

## Creating New Modules
1.  Define purpose and check for existing modules that might fit.
2.  Use convention plugins.
3.  Add to `settings.gradle.kts`.
4.  Create `README.md`.
5.  Verify graph: `./gradlew :app:assertModuleGraph`.
