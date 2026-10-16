package soft.divan.financemanager.core.database.holder

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * [DatabaseHolder] с арендами: каждая операция держит базу открытой, пока не закончится.
 *
 * ### Аренды
 * [withDatabase] увеличивает счётчик активных операций, [close] сначала перестаёт выдавать новые
 * аренды, затем ждёт, пока счётчик станет нулём, и только потом закрывает базу. Проверка «база
 * открыта» и захват аренды атомарны — закрытие не может вклиниться между ними.
 *
 * ### Вложенные вызовы
 * Аренда кладётся в контекст корутины. Вызов изнутри блока (репозиторий внутри транзакции) видит
 * её и работает с той же базой без новой аренды: иначе закрытие, ждущее внешнюю аренду,
 * отказало бы внутренней, и транзакция упала бы посередине.
 *
 * @param dispatcher где открывать и закрывать базу — это дисковые операции.
 */
class RoomDatabaseHolder(
    private val factory: DatabaseFactory,
    private val files: DatabaseFiles,
    private val dispatcher: CoroutineDispatcher
) : DatabaseHolder {

    /** Открытие, закрытие и стирание идут строго по очереди. */
    private val lifecycle = Mutex()

    /** Защищает связку «база доступна + счётчик аренд». */
    private val leaseLock = Any()

    private val _state = MutableStateFlow(DatabaseState.CLOSED)
    override val state: StateFlow<DatabaseState> = _state.asStateFlow()

    /** База, доступная для новых операций; `null`, пока она закрыта или закрывается. */
    private val available = MutableStateFlow<FinanceManagerDatabase?>(null)
    private val activeLeases = MutableStateFlow(0)

    /** Открытая база; меняется только под [lifecycle]. */
    private var opened: OpenedDatabase? = null

    override suspend fun open(key: ByteArray) {
        requireOutsideLease()
        lifecycle.withLock {
            if (opened != null) {
                key.fill(0)
                Log.w(TAG, "Database is already open")
                return
            }
            val database = withContext(dispatcher) { factory.open(key) }
            opened = database
            synchronized(leaseLock) {
                available.value = database.database
                _state.value = DatabaseState.OPEN
            }
            Log.i(TAG, "Database opened")
        }
    }

    override suspend fun close() {
        requireOutsideLease()
        lifecycle.withLock { closeLocked() }
    }

    override suspend fun wipe() {
        requireOutsideLease()
        lifecycle.withLock {
            try {
                closeLocked()
            } finally {
                // Стирание не должно зависеть от того, чисто ли закрылась база
                withContext(NonCancellable + dispatcher) { files.delete() }
            }
            Log.i(TAG, "Database files deleted")
        }
    }

    override suspend fun <T> withDatabase(block: suspend (FinanceManagerDatabase) -> T): T {
        val outer = currentCoroutineContext()[DatabaseLease]
        if (outer != null && outer.holder === this && outer.active) {
            return block(outer.database)
        }
        val lease = acquire()
        return try {
            withContext(lease) { block(lease.database) }
        } finally {
            release(lease)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T> observe(block: (FinanceManagerDatabase) -> Flow<T>): Flow<T> =
        available.flatMapLatest { database -> database?.let(block) ?: emptyFlow() }

    private suspend fun closeLocked() {
        val database = opened ?: return
        synchronized(leaseLock) {
            available.value = null
            _state.value = DatabaseState.CLOSING
        }
        // Закрытие доводится до конца даже при отмене вызывающего и при сбое самого close():
        // иначе база осталась бы в CLOSING навсегда, а следующее open() сочло бы её открытой
        withContext(NonCancellable) {
            activeLeases.first { it == 0 }
            try {
                withContext(dispatcher) { database.close() }
            } finally {
                opened = null
                _state.value = DatabaseState.CLOSED
            }
        }
        Log.i(TAG, "Database closed")
    }

    private fun acquire(): DatabaseLease = synchronized(leaseLock) {
        val database = available.value ?: throw DatabaseLockedException()
        activeLeases.update { it + 1 }
        DatabaseLease(this, database)
    }

    private fun release(lease: DatabaseLease) {
        lease.active = false
        activeLeases.update { it - 1 }
    }

    /**
     * Закрыть базу изнутри собственной аренды нельзя: закрытие ждало бы окончания той самой
     * операции, которая его вызвала, — вечная взаимная блокировка.
     */
    private suspend fun requireOutsideLease() {
        val lease = currentCoroutineContext()[DatabaseLease]
        check(lease == null || lease.holder !== this || !lease.active) {
            "Database lifecycle cannot be changed from inside withDatabase"
        }
    }

    /** Аренда базы, доступная вложенным вызовам через контекст корутины. */
    private class DatabaseLease(
        val holder: RoomDatabaseHolder,
        val database: FinanceManagerDatabase
    ) : AbstractCoroutineContextElement(DatabaseLease) {

        /** Сброшен после окончания аренды: отцепившаяся корутина не должна ею пользоваться. */
        @Volatile
        var active: Boolean = true

        companion object Key : CoroutineContext.Key<DatabaseLease>
    }

    private companion object {
        const val TAG = "DatabaseHolder"
    }
}
