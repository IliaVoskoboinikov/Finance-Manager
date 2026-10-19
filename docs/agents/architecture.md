# Architecture Guidelines

## Core Principles
*   **Clean Architecture:** Maintain strict separation between Data, Domain, and Presentation layers.
*   **MVVM:** Use ViewModel to manage UI state and handle business logic via UseCases.
*   **Unidirectional Data Flow (UDF):** State flows down, events flow up.
*   **Offline-First:** Room database is the Single Source of Truth (SSOT). Network is for synchronization.

## Layer Responsibilities
*   **Presentation (`feature:*:impl`):** Compose screens and ViewModels. ViewModels map Domain models to UiState.
    A feature's `:impl` may also hold feature-local `domain` (use cases, repository interfaces) and `data`
    (repository implementations, e.g. over DataStore) packages; the same layer rules apply inside it.
*   **Domain (`core:domain`):** Pure Kotlin. Contains Entities, UseCases, and Repository interfaces. No dependencies on Android or Data layers.
*   **Data (`core:data`, `core:database`, `core:network`):** Implements Repository interfaces. Handles DTO→Entity→Domain mapping.

## Repeated Taps
*   Every ViewModel action that writes (save, delete, sign in, clear data) is guarded against a
    repeated call **by state, not by a timer**: while a write runs, a new one does not start
    (`writeJob?.isActive`). If the write is not idempotent, the screen also stops accepting
    actions after success, while it is closing (a sticky flag like `isWriteCompleted`). An
    "in progress" flag alone is not enough: a Room write takes milliseconds, so the second tap
    arrives after success but before the screen closes. A retry is allowed after a failure.
    Reference: `TransactionViewModel.launchWrite`.
*   Don't make a sticky flag for an idempotent action (e.g. guest sign-in): a one-shot event can
    be lost, and a repeated tap is then the only way forward.
*   A dialog answer is accepted only while the dialog is shown (`AuthViewModel.onMergeConfirm`);
    a long operation sets its "busy" state synchronously, before the first suspension, and
    other actions check it (`AuthViewModel.idleContent`).
*   The UI also disables the button while the action runs (`TopBarModel.isActionEnabled`), but
    that is only feedback: the real guard lives in the ViewModel.
*   Launching an external Activity (`ActivityResultLauncher.launch`) is guarded by a
    `rememberSaveable` flag reset in the result callback.
*   Navigation is guarded in `Navigator` (see `navigation.md`). Every guard needs a test: two
    calls in a row → the use case is called once.

## Feature Boundaries
*   Features must be isolated. Communication between features happens through `:api` modules.
*   **Refactoring Restriction:** Do not rename public APIs or change package structures without explicit request.

## Safety
*   Avoid "God" classes.
*   Business logic is forbidden in the UI layer.
*   Never expose Retrofit `Response` or Room `Entity` objects to the UI layer.
