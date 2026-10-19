package soft.divan.financemanager.core.data.vault

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.KeysetStore
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Общая часть хранилища ключей базы: одна очередь на все операции с ключами, наблюдаемое
 * состояние данных и примитивы, которыми пользуются [LocalDataVault], [ProtectionLevelSwitcher]
 * и [BiometricVault].
 *
 * ### Смена набора ключей без потери данных
 * Новый набор собирается в памяти, **проверяется** ([verify] — разворачивается обратно в тот же
 * ключ базы), записывается целиком и только потом удаляются ключи Keystore прежнего набора
 * ([commit]). Процесс умер до записи — действует старый набор, после — новый; ключи, оставшиеся
 * без набора, убирает [deleteOrphans] при следующем старте.
 */
@Singleton
class VaultCore @Inject constructor(
    val holder: DatabaseHolder,
    val store: KeysetStore,
    private val keys: KeystoreKeys,
    private val envelope: DekEnvelope,
    @param:IoDispatcher private val io: CoroutineDispatcher
) {

    private val mutex = Mutex()
    private val _state = MutableStateFlow<LocalDataState>(LocalDataState.Initializing)

    /** Доступны ли данные. */
    val state: StateFlow<LocalDataState> = _state.asStateFlow()

    /** Операции с ключами идут строго по одной и вне главного потока. */
    suspend fun <T> exclusive(block: suspend () -> T): T =
        mutex.withLock { withContext(io) { block() } }

    /** Публикует новое состояние данных. */
    fun publish(state: LocalDataState) {
        _state.value = state
    }

    /** Сохранённый набор или `null`, если его нет или он не читается. */
    suspend fun readKeysetOrNull(): Keyset? = try {
        store.read()
    } catch (e: KeyUnavailableException) {
        Log.e(TAG, "Keyset is unreadable", e)
        null
    } catch (e: IOException) {
        Log.e(TAG, "Keyset is unreadable", e)
        null
    }

    /**
     * Открывает базу ключом [dek], развёрнутым по PIN или биометрии, и сбрасывает учёт ошибок PIN.
     * Массив переходит во владение холдера; если база уже открыта, он просто затирается.
     *
     * Открытие может сорваться и с верным ключом — испорченный файл, переполненный диск. Для
     * пользователя это то же, что потеря ключа: данные открыть не удалось, а экран восстановления
     * предлагает и повторить, и стереть. Ошибки PIN сбрасываются и тогда: PIN был верный.
     *
     * @return `false` — база не открылась, состояние — [LocalDataState.KeyLost].
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun unlockDatabase(dek: ByteArray): Boolean {
        val opened = try {
            holder.open(dek)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Database cannot be opened with the unsealed key", e)
            false
        }
        store.writeAttempts(PinAttempts())
        publish(if (opened) LocalDataState.Open else LocalDataState.KeyLost)
        return opened
    }

    /**
     * Проверяет, что [next] разворачивается в тот же [dek]. Новые ключи Keystore неудачного
     * набора удаляются сразу — сохранённый набор на них не ссылается.
     *
     * @throws KeyUnavailableException набор не разворачивается.
     */
    fun verify(next: Keyset, dek: ByteArray, pin: CharArray?, previous: Keyset?) {
        val unsealed = runCatching {
            if (next.level == KeyLevel.PIN) {
                envelope.openWithPin(next, requireNotNull(pin))
            } else {
                envelope.openWithoutUser(next)
            }
        }.getOrNull()
        val matches = unsealed != null && unsealed.contentEquals(dek)
        unsealed?.fill(0)
        if (!matches) {
            dropUnreferenced(next, previous)
            throw KeyUnavailableException("New keyset does not unseal the database key")
        }
    }

    /** Атомарно заменяет набор и удаляет ключи Keystore, на которые он больше не ссылается. */
    suspend fun commit(previous: Keyset?, next: Keyset) {
        store.write(next)
        dropUnreferenced(previous, next)
    }

    /** Удаляет ключи базы в Keystore, на которые не ссылается [keyset] (`null` — все). */
    fun deleteOrphans(keyset: Keyset?) {
        val referenced = keyset?.aliases().orEmpty()
        keys.aliases(DekEnvelope.KEY_PREFIX).minus(referenced).forEach(keys::delete)
    }

    /** Удаляет ключи [stale], которых нет в [kept]. */
    fun dropUnreferenced(stale: Keyset?, kept: Keyset?) {
        stale?.aliases().orEmpty().minus(kept?.aliases().orEmpty()).forEach(keys::delete)
    }

    /** Удаляет один ключ Keystore — например, созданный для операции, которая не состоялась. */
    fun deleteKey(alias: String) {
        keys.delete(alias)
    }

    private companion object {
        const val TAG = "VaultCore"
    }
}
