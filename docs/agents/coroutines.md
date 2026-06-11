# Coroutines & Concurrency

## Principles
*   **Structured Concurrency:** Always use a `CoroutineScope` (e.g., `viewModelScope`).
*   **Never use GlobalScope.**

## Dispatchers
*   **Dispatcher Injection:** Inject `CoroutineDispatcher` into classes that use them. Do not hardcode `Dispatchers.IO` or `Dispatchers.Default`.
*   Use `Dispatchers.Main` for UI operations, `Dispatchers.IO` for disk/network, and `Dispatchers.Default` for CPU-intensive work.

## Flow
*   Use `Flow` for data streams.
*   Use `StateFlow` for UI state in ViewModels.
*   Collect flows in the UI using `collectAsStateWithLifecycle()`.

## Safety
*   Never block the main thread.
*   Ensure suspend functions are main-safe.
