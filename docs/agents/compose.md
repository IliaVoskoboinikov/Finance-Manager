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
*   Previews are `private`: detekt compose-rules (`Compose:` in `config/detekt/detekt.yml`, `maxIssues: 0`)
    flag public ones (`PreviewPublic`), and `UnusedPrivateMember` ignores `@Preview`.

## Theme
*   The theme mode comes from DataStore asynchronously: never give it a default `initial`
    (it flashes the default theme for a frame) — `MainActivity` draws nothing until it is read.
*   The window theme is DayNight, and its `windowBackground` (`app` `values`/`values-night`
    `colors.xml`) mirrors the `background` of `FinanceManagerTheme` — keep them in sync.
*   The chosen mode also goes to `UiModeManager.setApplicationNightMode` (API 31+), so the
    system splash and the window are in that theme on a cold start; `uiMode` is in the
    Activity's `configChanges`, Compose redraws itself.

## Best Practices
*   **LaunchedEffect:** Use only for side effects triggered by state changes.
*   **rememberSaveable:** Use for UI state that should survive configuration changes (e.g., text field input).
*   **Accessibility:** Provide `contentDescription` for all interactive and informative elements.
