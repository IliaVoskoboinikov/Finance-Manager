package soft.divan.financemanager.core.database.holder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase

/**
 * Единственный владелец открытой базы: её можно открыть ключом, закрыть и стереть.
 *
 * Раньше база и DAO были синглтонами Hilt и жили столько же, сколько процесс. С шифрованием так
 * нельзя: на уровне PIN база закрыта до ввода PIN и закрывается снова после паузы в фоне, а
 * синглтоны держали бы мёртвый экземпляр. Поэтому DAO берутся из холдера на каждый вызов.
 *
 * Холдер ничего не знает о ключах — принимает готовые байты. Благодаря этому `:core:database` не
 * зависит от `:core:security`; ключами занимается хранилище в `:core:data`.
 */
interface DatabaseHolder {

    /** Текущее состояние: открыта, закрывается или закрыта. */
    val state: StateFlow<DatabaseState>

    /**
     * Открывает базу ключом [key] и сразу проверяет его первым обращением к файлу.
     *
     * Массив переходит во владение холдера: SQLCipher нужен ключ на каждое новое соединение, поэтому
     * он живёт, пока база открыта, и затирается при закрытии. Если база уже открыта, ключ
     * затирается и ничего не происходит.
     *
     * @throws Exception ключ не подходит к файлу или файл не открывается.
     */
    suspend fun open(key: ByteArray)

    /**
     * Закрывает базу, дождавшись окончания операций, которые уже идут. Новые операции с этого
     * момента получают [DatabaseLockedException].
     *
     * Закрывать базу под нагрузкой нельзя: Room после `close()` бросает `JobCancellationException`,
     * неотличимый от штатной отмены корутины.
     */
    suspend fun close()

    /** Закрывает базу как [close] и удаляет её файлы с диска. */
    suspend fun wipe()

    /**
     * Выполняет [block] с открытой базой, удерживая её от закрытия до конца блока.
     *
     * Вложенные вызовы (например, изнутри транзакции) используют ту же базу без новой аренды.
     *
     * @throws DatabaseLockedException база закрыта.
     */
    suspend fun <T> withDatabase(block: suspend (FinanceManagerDatabase) -> T): T

    /**
     * Реактивный запрос: пока база закрыта — молчит, после открытия подписывается на новый
     * экземпляр, при закрытии отписывается.
     */
    fun <T> observe(block: (FinanceManagerDatabase) -> Flow<T>): Flow<T>
}

/** Состояние базы в [DatabaseHolder]. */
enum class DatabaseState {
    CLOSED,

    /** Закрытие началось: новые операции не принимаются, идущие дорабатывают. */
    CLOSING,
    OPEN
}

/** База закрыта: данные на уровне PIN заблокированы или ещё не открыты после старта. */
class DatabaseLockedException : IllegalStateException("Local database is locked")
