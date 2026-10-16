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
*   **Language:** KDoc and code comments are written in Russian, like the existing code; identifiers stay English.

## Static Analysis
*   **Ktlint:** Run `./gradlew ktlintCheck`. Style `android_studio`, max line length 105 (`.editorconfig`).
*   **Detekt:** Run `./gradlew detekt`. `maxIssues: 0` and no baseline — fix new findings; numbers go to
    `const val` (`MagicNumber`).
*   **Lint:** Run `./gradlew lint`.
*   Fix all violations introduced by your changes.
