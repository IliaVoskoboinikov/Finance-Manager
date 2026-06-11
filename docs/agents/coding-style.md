# Coding Style & Quality

## Kotlin Idioms
*   **Immutability:** Use `val` for all properties unless mutation is strictly required. Use `data class` for models.
*   **Scope Functions:** Use `apply`, `run`, `with`, `let`, and `also` appropriately to improve readability.
*   **Extensions:** Use extension functions to keep classes focused and clean.
*   **Sealed Classes:** Use `sealed class` or `sealed interface` for UiStates and Result patterns.

## Naming Conventions
*   **Explicit Naming:** `GetAccountsUseCase`, `MyAccountsUiState`, `AccountRepositoryImpl`.
*   **Mappers:** Use `toDomain()`, `toEntity()`, `toDto()` for conversion functions.

## Quality Standards
*   **SOLID:** Follow Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion.
*   **Clean Code:** Functions should be small and do one thing.
*   **KDoc:** Mandatory for all public classes and functions.
*   **Comments:** Explain "why", not "what". Document complex business logic.

## Static Analysis
*   **Ktlint:** Run `./gradlew ktlintCheck`.
*   **Detekt:** Run `./gradlew detekt`.
*   **Lint:** Run `./gradlew lint`.
*   Fix all violations introduced by your changes.
