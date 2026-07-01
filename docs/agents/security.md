# Security

## Secrets Management
*   **Never** hardcode secrets, API keys, or tokens in source code.
*   Use `local.properties` or environment variables for local development (e.g. `API_TOKEN`,
    keystore credentials `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`).
*   Use CI/CD secrets for release builds. `release.jks` and `google-services.json` are gitignored.

## Data Protection
*   Encrypt sensitive values with the project's own **`CryptoManager`** (`core:security`) —
    AES/GCM backed by the Android KeyStore. Do **not** use the deprecated
    `androidx.security.crypto` (`EncryptedSharedPreferences` / `MasterKey`).
*   Current usage:
    *   **JWT tokens** — encrypted via `CryptoManager` and stored in DataStore (`core:auth`).
    *   **PIN** — hashed first (PBKDF2 + salt, `PinHasher`), then the hash is encrypted via
        `CryptoManager` and stored in SharedPreferences (`feature:security`).
*   Never store secrets/PII in plaintext and never lift a raw secret/PIN into the presentation
    layer — expose only a `verify(...)` operation, not the stored value.

## Transport
*   Production traffic MUST use **HTTPS**; do not ship `usesCleartextTraffic="true"`.
*   Cleartext HTTP is only acceptable against a local/test backend (current `BuildConfig.HOST`).

## Logging Restrictions
*   Use `android.util.Log` (the project's current convention).
*   `LoggingInterceptor` logs bodies at `BODY` level only in debug and `NONE` in release — no
    request/response bodies or `Authorization` headers end up in release logs.
*   **No PII, credentials, or tokens in logs** — including messages passed to `ErrorLogger`
    (Crashlytics).
