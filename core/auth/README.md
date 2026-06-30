# core:auth

## Purpose
The `core:auth` module handles authentication and session management for the application. It includes:
- Token management (storage and retrieval)
- Session state tracking (Authorized, Unauthorized, Guest)
- OkHttp Interceptors and Authenticators for handling JWT tokens and refresh logic.
- Authentication API service definition.

## Architecture
- **Data Layer**: Implements `TokenLocalDataSource` and `SessionLocalDataSource` using DataStore.
- **Domain Layer**: Defines `AuthStatus`, `SessionState` and `GetAuthStatusUseCase`.
- **Infrastructure**: Provides `TokenAuthenticator` and `GuestAccessInterceptor` for network safety and automatic token renewal.

## Dependencies
- `:core:common`
- `:core:security`
- `:core:database`
- `:core:domain`
- DataStore, Retrofit, OkHttp, Hilt.
