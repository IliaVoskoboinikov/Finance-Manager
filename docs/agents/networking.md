# Networking

## Retrofit Usage
*   Define API interfaces (`*ApiService`) in the data module that uses them — `core:data` (`api/`) or
    `core:auth`. `core:network` only provides the OkHttp/Retrofit setup and interceptors.
*   Use `Gson` for serialization (as configured in the project).

## DTO Isolation
*   **DTOs (Data Transfer Objects)** must never leave the data layer.
*   Map DTOs immediately in the data layer. Offline-first: DTOs become Room entities (`toEntity()`) in the
    repository or sync manager (`core:data` `sync/`), and Domain models are built from entities (`toDomain()`).

## Error Handling
*   Handle network failures (timeouts, no connection) explicitly.
*   Use a `Result` wrapper or similar pattern to communicate success/failure to the Domain layer.
*   Never expose Retrofit `Response` or `HttpException` to UseCases or ViewModels.

## Strategies
*   Implement retry logic for transient errors if necessary (use `RetryInterceptor` if available).
*   Connectivity is checked per request by `NetworkConnectionInterceptor` (`core:network`, throws
    `NoInternetException`). `NetworkMonitor` (`isOnline: Flow<Boolean>`) is for observing connectivity
    (e.g. `MainViewModel`), not for guarding individual requests.
