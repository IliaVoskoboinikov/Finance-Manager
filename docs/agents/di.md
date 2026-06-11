# Dependency Injection

## Hilt Rules
*   Use Dagger Hilt for all dependency injection.
*   Annotate Application with `@HiltAndroidApp`.
*   Annotate Activities/Fragments with `@AndroidEntryPoint`.

## Injection Patterns
*   **Constructor Injection:** Prefer constructor injection over field injection.
*   **Binds vs Provides:** Use `@Binds` for interface-to-implementation mapping. Use `@Provides` only for external library classes or complex initialization.

## Organization
*   Place Hilt modules in the `:impl` module of a feature or in the appropriate `core` module.
*   Use `@InstallIn(SingletonComponent::class)` for app-wide dependencies and `@InstallIn(ViewModelComponent::class)` for ViewModel-scoped ones.
