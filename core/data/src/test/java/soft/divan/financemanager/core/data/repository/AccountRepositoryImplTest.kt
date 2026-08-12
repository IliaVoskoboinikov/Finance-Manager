package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.outbox.OutboxEnqueuer
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountStatus
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.time.Instant

/**
 * Тесты [AccountRepositoryImpl].
 *
 * Репозиторий возвращает результат из локальной БД сразу, а синхронизацию с сервером пускает
 * в фон через [AppCoroutineContext]. Чтобы проверять это детерминированно, вместо реального
 * контекста подставлен [RecordingAppCoroutineContext]: он не выполняет фоновые блоки сразу, а
 * записывает их — тест сперва проверяет синхронный результат метода, затем вызывает `runAll()`
 * и проверяет побочные эффекты (обращения к [syncManager]).
 */
class AccountRepositoryImplTest {

    private val remoteDataSource = mockk<AccountRemoteDataSource>()
    private val localDataSource = mockk<AccountLocalDataSource>(relaxUnitFun = true)
    private val transactionLocalDataSource = mockk<TransactionLocalDataSource>()
    private val syncManager = mockk<AccountSyncManager>(relaxed = true)
    private val outboxEnqueuer = mockk<OutboxEnqueuer>(relaxed = true)
    private val appCoroutineContext = RecordingAppCoroutineContext()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    /** Выполняет блок как есть: атомарность проверяется отдельно, на реальном Room. */
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }

    private val repository = AccountRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        transactionLocalDataSource = transactionLocalDataSource,
        syncManager = syncManager,
        transactionRunner = transactionRunner,
        outboxEnqueuer = outboxEnqueuer,
        appCoroutineContext = appCoroutineContext,
        errorLogger = errorLogger
    )

    private val createdAt = "2024-01-01T00:00:00Z"
    private val updatedAt = "2024-02-01T12:30:00Z"

    private fun account(id: String = "local-1") = Account(
        id = id,
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )

    private fun entity(
        localId: String = "local-1",
        serverId: String? = "server-1",
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        status: String = "Active"
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        status = status
    )

    /* ---------- create ---------- */

    @Test
    fun `create stores entity with PENDING_CREATE and returns Success`() = runTest {
        val saved = slot<AccountEntity>()
        coEvery { localDataSource.create(capture(saved)) } returns Unit

        val result = repository.create(account())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(saved.captured.localId).isEqualTo("local-1")
        assertThat(saved.captured.serverId).isNull()
        assertThat(saved.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
        assertThat(saved.captured.balance).isEqualTo("100.50")
    }

    @Test
    fun `create enqueues an outbox create operation`() = runTest {
        coEvery { localDataSource.create(any()) } returns Unit

        repository.create(account())

        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-1",
                operation = OutboxOperation.CREATE,
                targetServerId = null,
                body = any()
            )
        }
    }

    @Test
    fun `create returns Failure when local insert throws`() = runTest {
        val boom = IllegalStateException("db")
        coEvery { localDataSource.create(any()) } throws boom

        val result = repository.create(account())

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    /* ---------- getAll ---------- */

    @Test
    fun `getAll emits domain accounts and skips PENDING_DELETE`() = runTest {
        every { localDataSource.getAll() } returns flowOf(
            listOf(
                entity(localId = "a1"),
                entity(localId = "a2", syncStatus = SyncStatus.PENDING_DELETE)
            )
        )

        val result = repository.getAll().first()

        val success = result as DomainResult.Success
        assertThat(success.data).hasSize(1)
        assertThat(success.data.first().id).isEqualTo("a1")
    }

    @Test
    fun `getAll skips Deleted accounts but keeps Hidden`() = runTest {
        every { localDataSource.getAll() } returns flowOf(
            listOf(
                entity(localId = "a1"),
                entity(localId = "a2", status = "Deleted"),
                entity(localId = "a3", status = "Hidden")
            )
        )

        val result = repository.getAll().first()

        val success = result as DomainResult.Success
        assertThat(success.data.map { it.id }).containsExactly("a1", "a3")
    }

    @Test
    fun `getAll launches background pull of server data`() = runTest {
        every { localDataSource.getAll() } returns flowOf(emptyList())

        repository.getAll()
        appCoroutineContext.runAll()

        coVerify(exactly = 1) { syncManager.pullServerData() }
    }

    @Test
    fun `getAll emits Failure when local flow fails`() = runTest {
        val boom = RuntimeException("query failed")
        every { localDataSource.getAll() } returns flow { throw boom }

        val result = repository.getAll().first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    /* ---------- getById ---------- */

    @Test
    fun `getById returns local account immediately`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity()

        val result = repository.getById("local-1")

        val success = result as DomainResult.Success
        assertThat(success.data.id).isEqualTo("local-1")
        assertThat(success.data.balance).isEqualByComparingTo(BigDecimal("100.50"))
    }

    @Test
    fun `getById returns NoData when account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.getById("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        assertThat(appCoroutineContext.launchCount).isZero()
    }

    @Test
    fun `getById propagates local db failure`() = runTest {
        val boom = IllegalStateException("db is down")
        coEvery { localDataSource.getByLocalId("local-1") } throws boom

        val result = repository.getById("local-1")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    @Test
    fun `getById refreshes synced account from server in background`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = "server-1")
        coEvery { remoteDataSource.getById("server-1") } returns Response.success(
            AccountDto(
                id = "server-1",
                userId = "u1",
                name = "Fresh",
                balance = BigDecimal("200.00"),
                currencyId = "rub-id",
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        )
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        repository.getById("local-1")
        appCoroutineContext.runAll()

        assertThat(updated.captured.localId).isEqualTo("local-1")
        assertThat(updated.captured.name).isEqualTo("Fresh")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `getById does not touch local db when server refresh fails`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = "server-1")
        coEvery { remoteDataSource.getById("server-1") } throws RuntimeException("offline")

        repository.getById("local-1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `getById does not chase an unsynced account`() = runTest {
        // Создание такого счёта уже стоит в очереди — догонять его отдельным запросом незачем
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = null)

        repository.getById("local-1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { remoteDataSource.getById(any()) }
        coVerify(exactly = 0) { outboxEnqueuer.enqueue(any(), any(), any(), any(), any()) }
    }

    /* ---------- update ---------- */

    @Test
    fun `update returns NoData when account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.update(account(id = "missing"))

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `update of synced account stores PENDING_UPDATE and enqueues update`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = "server-1")
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(account().copy(name = "Renamed"))

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.name).isEqualTo("Renamed")
        assertThat(updated.captured.serverId).isEqualTo("server-1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "server-1",
                body = any()
            )
        }
    }

    @Test
    fun `update of unsynced account addresses the operation by its client id`() = runTest {
        // serverId ещё нет, но сервер узнает счёт по localId, с которым ушло создание
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = null)
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(account())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "local-1",
                body = any()
            )
        }
    }

    /* ---------- updateBalanceLocal ---------- */

    @Test
    fun `updateBalanceLocal changes only balance`() = runTest {
        val local = entity(serverId = "server-1")
        coEvery { localDataSource.getByLocalId("local-1") } returns local
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.updateBalanceLocal("local-1", BigDecimal("77.70"))

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured).isEqualTo(local.copy(balance = "77.70"))
        assertThat(appCoroutineContext.launchCount).isZero()
    }

    @Test
    fun `updateBalanceLocal returns NoData when account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.updateBalanceLocal("missing", BigDecimal.ONE)

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    /* ---------- delete ---------- */

    @Test
    fun `delete without transactions keeps status and marks PENDING_DELETE`() = runTest {
        val local = entity(serverId = "server-1")
        coEvery { localDataSource.getByLocalId("local-1") } returns local
        coEvery { transactionLocalDataSource.getByAccountId("local-1") } returns emptyList()
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.delete("local-1")

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        assertThat(updated.captured.status).isEqualTo(AccountStatus.Active.name)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-1",
                operation = OutboxOperation.DELETE,
                targetServerId = "server-1",
                body = null
            )
        }
    }

    @Test
    fun `delete sets Deleted status via syncDelete when it has transactions`() = runTest {
        val local = entity(serverId = "server-1")
        coEvery { localDataSource.getByLocalId("local-1") } returns local
        coEvery { transactionLocalDataSource.getByAccountId("local-1") } returns
            listOf(mockk<TransactionEntity>())
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.delete("local-1")

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.status).isEqualTo(AccountStatus.Deleted.name)
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        // Архивация уходит тем же DELETE — отдельного обновления на сервер быть не должно
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.ACCOUNT,
                entityLocalId = "local-1",
                operation = OutboxOperation.DELETE,
                targetServerId = "server-1",
                body = null
            )
        }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), OutboxOperation.UPDATE, any(), any())
        }
    }

    @Test
    fun `delete returns Failure when transaction lookup fails`() = runTest {
        val boom = IllegalStateException("db")
        coEvery { localDataSource.getByLocalId("local-1") } returns entity()
        coEvery { transactionLocalDataSource.getByAccountId("local-1") } throws boom

        val result = repository.delete("local-1")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        assertThat(appCoroutineContext.launchCount).isZero()
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `delete returns NoData when account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.delete("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }
}
