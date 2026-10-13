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

## Повторные нажатия
*   Любое действие ViewModel с записью (сохранение, удаление, вход, очистка данных) защищено от
    повторного вызова **состоянием, а не таймером**: пока запись идёт, новая не запускается
    (`writeJob?.isActive`), а после успеха, когда экран закрывается, действия больше не
    принимаются (липкий флаг вроде `isWriteCompleted`). Флага «идёт» мало: запись в Room занимает
    миллисекунды, и второй тап приходит уже после успеха, но до закрытия экрана. После ошибки
    повтор разрешён. Образец — `TransactionViewModel.launchWrite`.
*   Ответ на диалог принимается, только пока диалог открыт (`AuthViewModel.onMergeConfirm`).
*   UI дополнительно гасит кнопку на время действия (`TopBarModel.isActionEnabled`), но это
    только обратная связь: главная защита — во ViewModel.
*   Запуск внешней Activity (`ActivityResultLauncher.launch`) защищается флагом в
    `rememberSaveable`, который сбрасывается в колбэке результата.
*   Навигация защищена в `Navigator` (см. `navigation.md`). Тесты на каждую защиту обязательны:
    два вызова подряд → use case вызван один раз.

## Feature Boundaries
*   Features must be isolated. Communication between features happens through `:api` modules.
*   **Refactoring Restriction:** Do not rename public APIs or change package structures without explicit request.

## Safety
*   Avoid "God" classes.
*   Business logic is forbidden in the UI layer.
*   Never expose Retrofit `Response` or Room `Entity` objects to the UI layer.
