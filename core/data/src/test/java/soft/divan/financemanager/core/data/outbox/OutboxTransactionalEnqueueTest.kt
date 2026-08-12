package soft.divan.financemanager.core.data.outbox

import androidx.room.Room
import com.google.gson.Gson
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
import soft.divan.financemanager.core.data.transaction.PostCommitSyncQueue
import soft.divan.financemanager.core.data.transaction.impl.RoomTransactionRunner
import soft.divan.financemanager.core.data.transaction.rollbackOnError
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Ключевая гарантия Transactional Outbox на реальном in-memory Room: запись в очередь и доменное
 * изменение фиксируются **одной** транзакцией.
 *
 * Проверяется обе стороны инварианта — при commit сохраняются оба факта, при rollback не остаётся
 * ни одного. Именно это делает невозможной отправку операции, которой нет локально.
 */
@RunWith(RobolectricTestRunner::class)
class OutboxTransactionalEnqueueTest {

    private class NoopAppCoroutineContext : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit

        override suspend fun launchSync(block: suspend () -> Unit) {
            currentCoroutineContext()[PostCommitSyncQueue]?.add(block)
        }
    }

    /** Копит задиспатченные после commit действия, чтобы тест сам решал, когда их выполнить. */
    private class CollectingAppCoroutineContext : AppCoroutineContext {
        val dispatched = mutableListOf<suspend () -> Unit>()

        override fun launch(block: suspend CoroutineScope.() -> Unit) {
            dispatched.add { CoroutineScope(EmptyCoroutineContext).block() }
        }

        override suspend fun launchSync(block: suspend () -> Unit) {
            val queue = currentCoroutineContext()[PostCommitSyncQueue]
            if (queue != null) queue.add(block) else block()
        }
    }

    private lateinit var db: FinanceManagerDatabase
    private lateinit var runner: RoomTransactionRunner
    private lateinit var enqueuer: OutboxEnqueuer

    private val now = Instant.parse("2024-05-01T10:00:00Z")

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            FinanceManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        runner = RoomTransactionRunner(db, NoopAppCoroutineContext())
        enqueuer = OutboxEnqueuer(
            localDataSource = OutboxLocalDataSourceImpl(db.outboxDao()),
            gson = Gson(),
            clock = Clock.fixed(now, ZoneOffset.UTC),
            appCoroutineContext = NoopAppCoroutineContext(),
            processor = { mockk(relaxed = true) }
        )
    }

    @After
    fun tearDown() = db.close()

    private fun account(localId: String = "local-a1") = AccountEntity(
        localId = localId,
        serverId = null,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.PENDING_CREATE
    )

    private suspend fun enqueueCreate(localId: String) = enqueuer.enqueue(
        entityType = OutboxEntityType.ACCOUNT,
        entityLocalId = localId,
        operation = OutboxOperation.CREATE,
        body = mapOf("id" to localId, "name" to "Cash")
    )

    private suspend fun queued() = db.outboxDao().getReadyToSend(
        now = now.toEpochMilli(),
        staleBefore = 0,
        limit = 10
    )

    @Test
    fun `commit persists both the domain row and its outbox entry`() = runTest {
        runner.runInTransaction {
            db.accountDao().insert(account())
            enqueueCreate("local-a1")
            DomainResult.Success(Unit)
        }

        assertThat(db.accountDao().getByLocalId("local-a1")).isNotNull()
        assertThat(queued().map { it.entityLocalId }).containsExactly("local-a1")
    }

    @Test
    fun `rollback drops the outbox entry together with the domain row`() = runTest {
        runCatching {
            runner.runInTransaction {
                db.accountDao().insert(account())
                enqueueCreate("local-a1")
                error("сбой уже после постановки операции в очередь")
            }
        }

        // Ни данных, ни намерения их отправить — фантомная операция невозможна
        assertThat(db.accountDao().getByLocalId("local-a1")).isNull()
        assertThat(queued()).isEmpty()
    }

    @Test
    fun `failure result rolls the outbox entry back as well`() = runTest {
        // Штатный путь отката в use case'ах: rollbackOnError() на Failure
        val result = runner.runInTransaction<DomainResult<Unit>> {
            db.accountDao().insert(account())
            enqueueCreate("local-a1")
            DomainResult.Success(DomainResult.Failure(DomainError.NoData).rollbackOnError())
        }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        assertThat(db.accountDao().getByLocalId("local-a1")).isNull()
        assertThat(queued()).isEmpty()
    }

    @Test
    fun `queue processing is deferred until after the commit`() = runTest {
        // Ради этого и живёт PostCommitSyncQueue: разбор очереди внутри транзакции увидел бы
        // ещё не зафиксированную запись (то же соединение Room) и отправил бы её на сервер до
        // commit — при откате получился бы ровно тот фантом, от которого защищает outbox.
        val context = CollectingAppCoroutineContext()
        val localRunner = RoomTransactionRunner(db, context)
        val processor = mockk<OutboxProcessor>(relaxed = true)
        val localEnqueuer = OutboxEnqueuer(
            localDataSource = OutboxLocalDataSourceImpl(db.outboxDao()),
            gson = Gson(),
            clock = Clock.fixed(now, ZoneOffset.UTC),
            appCoroutineContext = context,
            processor = { processor }
        )

        var dispatchedInsideTransaction = false
        localRunner.runInTransaction {
            db.accountDao().insert(account())
            localEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-a1",
                operation = OutboxOperation.CREATE,
                body = mapOf("id" to "local-a1")
            )
            dispatchedInsideTransaction = context.dispatched.isNotEmpty()
            DomainResult.Success(Unit)
        }

        assertThat(dispatchedInsideTransaction).isFalse()
        coVerify(exactly = 0) { processor.process() }

        // После commit действие задиспатчено — выполняем и убеждаемся, что это разбор очереди
        assertThat(context.dispatched).hasSize(1)
        context.dispatched.forEach { it() }
        coVerify(exactly = 1) { processor.process() }
    }

    @Test
    fun `entries keep enqueue order across separate transactions`() = runTest {
        runner.runInTransaction {
            db.accountDao().insert(account("local-a1"))
            enqueueCreate("local-a1")
            DomainResult.Success(Unit)
        }
        runner.runInTransaction {
            db.accountDao().insert(account("local-a2"))
            enqueueCreate("local-a2")
            DomainResult.Success(Unit)
        }

        assertThat(queued().map { it.entityLocalId })
            .containsExactly("local-a1", "local-a2")
    }
}
