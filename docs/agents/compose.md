# UI & Jetpack Compose

## Principles
*   **Stateless Composables:** Hoist state to the caller. Prefer passing values and lambdas.
*   **State Hoisting:** ViewModel should hold the `UiState`. Composables consume the state and emit events.
*   **Material 3:** Use Material 3 components and the theme from `core:uikit`.

## Reuse
*   Check `core:uikit` for existing components before creating new ones.
*   If a component is used in multiple features, move it to `core:uikit`.

## Previews
*   Every UI component and screen MUST have a `@Preview` composable.
*   Provide previews for Dark Mode and different font scales if relevant.

## Best Practices
*   **LaunchedEffect:** Use only for side effects triggered by state changes.
*   **rememberSaveable:** Use for UI state that should survive configuration changes (e.g., text field input).
*   **Accessibility:** Provide `contentDescription` for all interactive and informative elements.
