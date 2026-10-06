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
    *   **Local database** — always SQLCipher; the database key (DEK) is wrapped per protection
        level (`core:security` `DekEnvelope`) and stored in a `noBackupFilesDir` DataStore. Design:
        [`docs/encryption.md`](../encryption.md).
*   Never store secrets/PII in plaintext and never lift a raw secret/PIN into the presentation
    layer — expose only a `verify(...)` operation, not the stored value.

## Database Key Rules
*   **Keystore outside, PIN inside** on the PIN level: `AES-GCM(Keystore, AES-GCM(PIN key, DEK))`.
    Never reverse the layers — a PIN layer on the outside makes offline brute force possible.
*   **Never silently recreate** a missing or invalidated Keystore key for the database: that loses
    the data. Surface `KeyUnavailableException` → `LocalDataState.KeyLost` instead. Do not reuse
    `AndroidCryptoManager` (it recreates keys on errors) for database keys — use `KeystoreKeys`.
*   **Changing the keyset** goes through `VaultCore`: build → `verify` (unwraps to the same DEK) →
    atomic write → delete unreferenced Keystore aliases. Every wrap gets a new alias.
*   Keep key bytes in `ByteArray`/`CharArray` (never `String`) and wipe them (`fill(0)`) after use;
    the DEK and the biometric `Cipher` session never reach the presentation layer.
*   PIN attempts on the lock screen are counted **before** verification (`PinAttemptPolicy`);
    the 10th failure wipes the data and signs out. Sign-out/wipe must run outside `VaultCore`'s
    lock (sign-out itself calls the wipe).
*   Biometrics unlock the DEK only via `BiometricPrompt.CryptoObject` with `BIOMETRIC_STRONG`;
    an invalidated biometric key removes the biometric copy (PIN remains), it never wipes data.

## Screen & Backups
*   `FLAG_SECURE` is a user setting that is **on by default**; `MainActivity` adds it before the
    first frame and clears it only after reading the setting.
*   `data_extraction_rules.xml` / `backup_rules.xml` exclude every domain from cloud backup and
    device transfer (on Android 12+ `allowBackup="false"` does not disable device transfer).
*   The OkHttp response cache is disabled: API responses must not land in `cacheDir` as plain text.

## Transport
*   Production traffic MUST use **HTTPS**; do not ship `usesCleartextTraffic="true"`.
*   Cleartext HTTP is only acceptable against a local/test backend (current `BuildConfig.HOST`).

## Logging Restrictions
*   Use `android.util.Log` (the project's current convention).
*   `LoggingInterceptor` logs bodies at `BODY` level only in debug and `NONE` in release — no
    request/response bodies or `Authorization` headers end up in release logs.
*   **No PII, credentials, or tokens in logs** — including messages passed to `ErrorLogger`
    (Crashlytics).
