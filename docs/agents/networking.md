# Networking

## Retrofit Usage
*   Define API interfaces in `core:network` or appropriate data modules.
*   Use `Gson` for serialization (as configured in the project).

## DTO Isolation
*   **DTOs (Data Transfer Objects)** must never leave the data layer.
*   Map DTOs to Domain models immediately in the Repository implementation.

## Error Handling
*   Handle network failures (timeouts, no connection) explicitly.
*   Use a `Result` wrapper or similar pattern to communicate success/failure to the Domain layer.
*   Never expose Retrofit `Response` or `HttpException` to UseCases or ViewModels.

## Strategies
*   Implement retry logic for transient errors if necessary (use `RetryInterceptor` if available).
*   Check `NetworkMonitor` from `core:network` for connectivity status before making requests.
