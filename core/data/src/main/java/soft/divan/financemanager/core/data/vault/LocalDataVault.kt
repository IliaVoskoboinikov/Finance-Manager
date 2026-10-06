package soft.divan.financemanager.core.data.vault

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.database.holder.DatabaseFiles
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранилище ключа базы: создаёт его при первом запуске, открывает базу, запирает и стирает её.
 *
 * ```
 * Initializing ──L0/L1──> Open ──lock (L2)──> Locked ──PIN / биометрия──> Open
 *      │                   │                    │
 *      └──ключ утерян──> KeyLost ──wipe──> Open (новая база, уровень L1)
 * ```
 *
 * PIN проверяет [PinUnlocker], биометрию — [BiometricVault], смену уровня ведёт
 * [ProtectionLevelSwitcher]; общая блокировка и состояние — в [VaultCore].
 *
 * Сюда не входит выход из аккаунта: его запускает репозиторий уже после того, как хранилище
 * отпустило блокировку, — выход сам вызывает [wipe] через `DatabaseCleanupManager`, и вызов
 * изнутри блокировки ждал бы сам себя.
 */
@Singleton
class LocalDataVault @Inject constructor(
    private val core: VaultCore,
    private val envelope: DekEnvelope,
    private val files: DatabaseFiles
) {

    /** Доступны ли данные. */
    val state: StateFlow<LocalDataState> get() = core.state

    /**
     * Первичная инициализация; повторный вызов имеет смысл только после [LocalDataState.KeyLost]
     * (кнопка «повторить»), в остальных состояниях ничего не делает.
     */
    suspend fun initialize() = core.exclusive {
        val current = core.state.value
        if (current is LocalDataState.Initializing || current is LocalDataState.KeyLost) {
            core.publish(restore())
        }
    }

    /** Закрывает базу уровня [KeyLevel.PIN]; ключ из памяти уходит вместе с ней. */
    suspend fun lock() = core.exclusive {
        val keyset = core.readKeysetOrNull()
        if (keyset?.level == KeyLevel.PIN && core.state.value == LocalDataState.Open) {
            // Экран замка появляется сразу; сама база закроется, когда доработают идущие операции
            core.publish(LocalDataState.Locked(biometricEnabled = keyset.biometric != null))
            core.holder.close()
        }
    }

    /**
     * Крипто-стирание: ключи уничтожаются **первыми** — после этого файл уже нечитаем, даже если
     * процесс умрёт до удаления файлов. Затем создаётся пустая база с уровнем L1.
     */
    suspend fun wipe() = core.exclusive {
        core.store.clear()
        core.deleteOrphans(keyset = null)
        core.holder.wipe()
        Log.i(TAG, "Local data wiped")
        core.publish(createFresh())
    }

    private suspend fun restore(): LocalDataState {
        val keyset = readKeyset().getOrElse { return keyLost(it) }
        core.deleteOrphans(keyset)
        if (keyset == null && files.isPlaintext()) {
            // Файл от сборки без шифрования (набора ключей тогда ещё не было): SQLCipher его не
            // откроет, а реальных пользователей у приложения нет — база создаётся заново
            Log.w(TAG, "Replacing a plaintext database left by a pre-encryption build")
            core.holder.wipe()
        }
        return when {
            keyset == null && files.exists() -> keyLost(null)
            keyset == null -> createFresh()
            keyset.level == KeyLevel.PIN -> LocalDataState.Locked(keyset.biometric != null)
            else -> openWithoutUser(keyset)
        }
    }

    /**
     * Сохранённый набор (`null` — его нет) или ошибка чтения: испорченный набор или сбой диска.
     *
     * Сбой чтения — не «первый запуск» и не повод молча начать заново: без этой ветки исключение
     * ушло бы из [initialize], данные навсегда остались бы в `Initializing`, а пользователь видел
     * бы пустой экран без выхода. Как потеря ключа — экран восстановления предложит повторить.
     */
    private suspend fun readKeyset(): Result<Keyset?> = try {
        Result.success(core.store.read())
    } catch (e: KeyUnavailableException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }

    /** Первый запуск или состояние после стирания: новый ключ базы на уровне L1. */
    private suspend fun createFresh(): LocalDataState = attempt {
        val dek = envelope.newDek()
        val keyset = envelope.sealDevice(dek)
        core.verify(keyset, dek, pin = null, previous = null)
        // Набор — до файла базы: база без набора выглядела бы как потеря ключа
        core.commit(previous = null, next = keyset)
        core.holder.open(dek)
        LocalDataState.Open
    }

    private suspend fun openWithoutUser(keyset: Keyset): LocalDataState = attempt {
        core.holder.open(envelope.openWithoutUser(keyset))
        LocalDataState.Open
    }

    private fun keyLost(cause: Throwable?): LocalDataState {
        Log.e(TAG, "Database key is unavailable", cause)
        return LocalDataState.KeyLost
    }

    /**
     * Открытие базы падает не только на ключах: SQLCipher отвергает неподходящий ключ исключением
     * SQLite, диск может быть переполнен. Для пользователя это одно состояние — данные открыть не
     * удалось, — а экран восстановления предлагает и повторить, и стереть.
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun attempt(block: () -> LocalDataState): LocalDataState = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        keyLost(e)
    }

    private companion object {
        const val TAG = "LocalDataVault"
    }
}
