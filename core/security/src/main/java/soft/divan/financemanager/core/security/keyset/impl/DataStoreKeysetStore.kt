package soft.divan.financemanager.core.security.keyset.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.security.di.KeysetDataStore
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.KeysetStore
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keyset.SealedKey
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.util.Base64
import javax.inject.Inject

/**
 * [KeysetStore] на Preferences DataStore.
 *
 * DataStore переписывает файл целиком через временный файл и атомарное переименование, поэтому
 * одна транзакция `edit` — это атомарная запись всего набора. Файл лежит в `noBackupFilesDir`
 * (см. провайдер в `EncryptionModule`).
 *
 * Испорченное значение внутри файла не превращается в `null` — это выглядело бы как первый
 * запуск. Вместо этого — [KeyUnavailableException], и пользователь видит экран восстановления.
 * Испорченный файл целиком DataStore заменяет пустым (см. `EncryptionModule`), и отсутствие набора
 * рядом с существующей базой хранилище ключей тоже трактует как потерю ключа.
 */
class DataStoreKeysetStore @Inject constructor(
    @param:KeysetDataStore private val dataStore: DataStore<Preferences>
) : KeysetStore {

    override suspend fun read(): Keyset? {
        val prefs = dataStore.data.first()
        val levelName = prefs[LEVEL] ?: return null
        return try {
            Keyset(
                level = KeyLevel.valueOf(levelName),
                openKey = prefs[OPEN_KEY]?.let(::decode),
                sealed = sealedKey(prefs[SEALED_ALIAS], prefs[SEALED_BLOB]),
                pinSalt = prefs[PIN_SALT]?.let(::decode),
                pinIterations = prefs[PIN_ITERATIONS] ?: 0,
                biometric = sealedKey(prefs[BIO_ALIAS], prefs[BIO_BLOB])
            )
        } catch (e: IllegalArgumentException) {
            throw KeyUnavailableException("Stored keyset is corrupted", e)
        }
    }

    override suspend fun write(keyset: Keyset) {
        dataStore.edit { prefs ->
            KEYSET_KEYS.forEach { prefs.remove(it) }
            prefs[LEVEL] = keyset.level.name
            keyset.openKey?.let { prefs[OPEN_KEY] = encode(it) }
            keyset.sealed?.let { prefs.putSealed(SEALED_ALIAS, SEALED_BLOB, it) }
            keyset.pinSalt?.let { prefs[PIN_SALT] = encode(it) }
            if (keyset.pinIterations > 0) prefs[PIN_ITERATIONS] = keyset.pinIterations
            keyset.biometric?.let { prefs.putSealed(BIO_ALIAS, BIO_BLOB, it) }
        }
    }

    override fun observeLevel(): Flow<KeyLevel?> = dataStore.data
        .map { prefs -> prefs[LEVEL]?.let { runCatching { KeyLevel.valueOf(it) }.getOrNull() } }
        .distinctUntilChanged()

    override fun observeBiometric(): Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[BIO_ALIAS] != null && prefs[BIO_BLOB] != null }
        .distinctUntilChanged()

    override suspend fun readAttempts(): PinAttempts {
        val prefs = dataStore.data.first()
        return PinAttempts(
            failures = prefs[PIN_FAILURES] ?: 0,
            lockedUntilMillis = prefs[PIN_LOCKED_UNTIL] ?: 0,
            lastFailureAtMillis = prefs[PIN_LAST_FAILURE_AT] ?: 0
        )
    }

    override suspend fun writeAttempts(attempts: PinAttempts) {
        dataStore.edit { prefs ->
            prefs[PIN_FAILURES] = attempts.failures
            prefs[PIN_LOCKED_UNTIL] = attempts.lockedUntilMillis
            prefs[PIN_LAST_FAILURE_AT] = attempts.lastFailureAtMillis
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun sealedKey(alias: String?, blob: String?): SealedKey? =
        if (alias != null && blob != null) SealedKey(alias, decode(blob)) else null

    private fun MutablePreferences.putSealed(
        aliasKey: Preferences.Key<String>,
        blobKey: Preferences.Key<String>,
        sealed: SealedKey
    ) {
        this[aliasKey] = sealed.alias
        this[blobKey] = encode(sealed.blob)
    }

    private companion object {
        val LEVEL = stringPreferencesKey("level")
        val OPEN_KEY = stringPreferencesKey("open_key")
        val SEALED_ALIAS = stringPreferencesKey("sealed_alias")
        val SEALED_BLOB = stringPreferencesKey("sealed_blob")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val PIN_ITERATIONS = intPreferencesKey("pin_iterations")
        val BIO_ALIAS = stringPreferencesKey("bio_alias")
        val BIO_BLOB = stringPreferencesKey("bio_blob")

        val PIN_FAILURES = intPreferencesKey("pin_failures")
        val PIN_LOCKED_UNTIL = longPreferencesKey("pin_locked_until")
        val PIN_LAST_FAILURE_AT = longPreferencesKey("pin_last_failure_at")

        /** Ключи набора — без учёта попыток: их жизненный цикл отдельный. */
        val KEYSET_KEYS = listOf(
            LEVEL,
            OPEN_KEY,
            SEALED_ALIAS,
            SEALED_BLOB,
            PIN_SALT,
            PIN_ITERATIONS,
            BIO_ALIAS,
            BIO_BLOB
        )
    }
}

private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)
