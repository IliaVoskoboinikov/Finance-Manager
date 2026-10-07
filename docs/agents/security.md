# Security

## Secrets Management
*   **Never** hardcode secrets, API keys, or tokens in source code.
*   Build secrets (`API_TOKEN`, `YANDEX_CLIENT_ID`) are read from an environment variable, falling back to
    `local.properties` for local development. Keystore credentials (`KEYSTORE_PASSWORD` / `KEY_ALIAS` /
    `KEY_PASSWORD`) are read from environment variables only.
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
*   Current pre-release state: `BuildConfig.HOST` points to the test backend `http://yourflow.pro/` in both
    `debug` and `release` (`core/network/build.gradle.kts`), and `app/src/main/AndroidManifest.xml` sets
    `usesCleartextTraffic="true"`. Cleartext is deliberately allowed for this test backend only; switching
    to HTTPS before a production backend is tracked in `TODO.md`. Don't add new cleartext endpoints.

## Logging Restrictions
*   Use `android.util.Log` (the project's current convention).
*   `LoggingInterceptor` logs bodies at `BODY` level only in debug and `NONE` in release — no
    request/response bodies or `Authorization` headers end up in release logs.
*   **No PII, credentials, or tokens in logs** — including messages passed to `ErrorLogger`
    (Crashlytics).
