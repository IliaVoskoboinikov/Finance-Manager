# Security

## Secrets Management
*   **Never** hardcode secrets, API keys, or tokens in source code.
*   Use `local.properties` or environment variables for local development.
*   Use CI/CD secrets for production builds.

## Data Protection
*   Use `EncryptedSharedPreferences` or `SecurityCrypto` for sensitive user data.
*   Avoid logging sensitive information (PII, credentials).

## Logging Restrictions
*   Use `Timber` for logging.
*   Ensure logs are removed or disabled in release builds.
*   **No PII in logs.**
