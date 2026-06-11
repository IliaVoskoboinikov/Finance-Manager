# Architecture Guidelines

## Core Principles
*   **Clean Architecture:** Maintain strict separation between Data, Domain, and Presentation layers.
*   **MVVM:** Use ViewModel to manage UI state and handle business logic via UseCases.
*   **Unidirectional Data Flow (UDF):** State flows down, events flow up.
*   **Offline-First:** Room database is the Single Source of Truth (SSOT). Network is for synchronization.

## Layer Responsibilities
*   **Presentation (`feature:*:impl`):** Compose screens and ViewModels. ViewModels map Domain models to UiState.
*   **Domain (`core:domain`):** Pure Kotlin. Contains Entities, UseCases, and Repository interfaces. No dependencies on Android or Data layers.
*   **Data (`core:data`, `core:database`, `core:network`):** Implements Repository interfaces. Handles DTO-to-Domain mapping.

## Feature Boundaries
*   Features must be isolated. Communication between features happens through `:api` modules.
*   **Refactoring Restriction:** Do not rename public APIs or change package structures without explicit request.

## Safety
*   Avoid "God" classes.
*   Business logic is forbidden in the UI layer.
*   Never expose Retrofit `Response` or Room `Entity` objects to the UI layer.
