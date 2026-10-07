# Modularization & Dependency Rules

## Module Structure
*   **Feature Split:** Split features into `:api` and `:impl`.
    *   `:api`: Contains navigation keys (`@Serializable` `NavKey`), the `<Name>FeatureApi` interface, and minimal public models.
    *   `:impl`: Contains UI, ViewModels, and Hilt modules (plus feature-local `domain`/`data` packages where needed).
*   **Core Modules:** `core:*` modules must never depend on `feature:*` or `app`.

## Dependency Flow
*   `feature:*:impl` -> `feature:*:api`
*   `feature:*:impl` -> `core:*`
*   `feature:*:impl` -> `feature:other:api` (NEVER depend on another `:impl`).
*   `app` -> all `feature:*:api` and `feature:*:impl`.

## Build Logic & Conventions
*   **Convention Plugins:** Every module must use `soft.divan.*` convention plugins from `build-logic`.
    *   `soft.divan.core` for Android `core:*` modules (and `:sync`).
    *   `soft.divan.jvm.library` for pure Kotlin/JVM modules (`core:domain`, `:konsist`).
    *   `soft.divan.feature.api` for API modules.
    *   `soft.divan.feature.impl` for Implementation modules (already applies Compose and `soft.divan.hilt`).
    *   `soft.divan.android.app` for `:app` only.
    *   `soft.divan.hilt` is added next to `soft.divan.core` when the module uses Hilt.
    *   The rest (`soft.divan.android.base`, `.firebase`, `.module.graph`, `.check.conventions`, `.ruler`,
        `.dependency.guard`, `.build.time.tracker`) are applied internally by the plugins above — not by modules directly.
    *   Exception: `:lint` (custom lint checks) uses plain `java-library` + Kotlin JVM + `com.android.lint`.
*   **Module README:** Every module MUST contain a `README.md` describing its purpose and dependencies.

## Creating New Modules
1.  Define purpose and check for existing modules that might fit.
2.  Use convention plugins.
3.  Add to `settings.gradle.kts`.
4.  Create `README.md`.
5.  Verify graph: `./gradlew :app:assertModuleGraph`.
