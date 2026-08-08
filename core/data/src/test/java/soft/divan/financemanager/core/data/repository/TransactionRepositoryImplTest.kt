package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.outbox.OutboxEnqueuer
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.time.Instant

class TransactionRepositoryImplTest {

    private val remoteDataSource = mockk<TransactionRemoteDataSource>()
    private val localDataSource = mockk<TransactionLocalDataSource>(relaxUnitFun = true)
    private val accountLocalDataSource = mockk<AccountLocalDataSource>()
    private val categoryLocalDataSource = mockk<CategoryLocalDataSource>()
    private val syncManager = mockk<TransactionSyncManager>(relaxed = true)
    private val outboxEnqueuer = mockk<OutboxEnqueuer>(relaxed = true)

    /** Выполняет блок как есть: атомарность проверяется отдельно, на реальном Room. */
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val appCoroutineContext = RecordingAppCoroutineContext()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val repository = TransactionRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        accountLocalDataSource = accountLocalDataSource,
        categoryLocalDataSource = categoryLocalDataSource,
        syncManager = syncManager,
        transactionRunner = transactionRunner,
        outboxEnqueuer = outboxEnqueuer,
        appCoroutineContext = appCoroutineContext,
        errorLogger = errorLogger
    )

    private val createdAt = "2024-01-01T00:00:00Z"
    private val updatedAt = "2024-02-01T12:30:00Z"
    private val transactionDate = "2024-01-15T10:00:00Z"

    private fun transaction(id: String = "local-t1") = Transaction(
        id = id,
        accountLocalId = "local-a1",
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse(transactionDate),
        comment = "lunch",
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )

    private fun entity(
        localId: String = "local-t1",
        serverId: String? = "server-t1",
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = TransactionEntity(
        localId = localId,
        serverId = serverId,
        accountLocalId = "local-a1",
        type = TransactionType.EXPENSE.name,
        targetAccountLocalId = null,
        accountServerId = "server-a1",
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = transactionDate,
        comment = "lunch",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun accountEntity(serverId: String? = "server-a1") = AccountEntity(
        localId = "local-a1",
        serverId = serverId,
        name = "Cash",
        balance = "0",
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.SYNCED
    )

    /* ---------- create ---------- */

    @Test
    fun `create resolves account server id and stores PENDING_CREATE entity`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        val saved = slot<TransactionEntity>()
        coEvery { localDataSource.insert(capture(saved)) } returns Unit

        val result = repository.create(transaction())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(saved.captured.localId).isEqualTo("local-t1")
        assertThat(saved.captured.serverId).isNull()
        assertThat(saved.captured.accountServerId).isEqualTo("server-a1")
        assertThat(saved.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
    }

    @Test
    fun `create enqueues an outbox create operation`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery { localDataSource.insert(any()) } returns Unit

        repository.create(transaction())

        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                operation = OutboxOperation.CREATE,
                targetServerId = null,
                body = any()
            )
        }
    }

    @Test
    fun `create keeps null account server id when account is not synced`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns
            accountEntity(serverId = null)
        val saved = slot<TransactionEntity>()
        coEvery { localDataSource.insert(capture(saved)) } returns Unit

        repository.create(transaction())

        assertThat(saved.captured.accountServerId).isNull()
    }

    @Test
    fun `create returns Failure when local insert throws`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        val boom = IllegalStateException("db")
        coEvery { localDataSource.insert(any()) } throws boom

        val result = repository.create(transaction())

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    /* ---------- getByAccountAndPeriod ---------- */

    @Test
    fun `getByAccountAndPeriod emits domain transactions and skips PENDING_DELETE`() = runTest {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T00:00:00Z")
        every {
            localDataSource.getByAccountAndPeriod(
                accountId = "local-a1",
                startDate = ApiDateMapper.toApiDate(start),
                endDate = ApiDateMapper.toApiDate(end)
            )
        } returns flowOf(
            listOf(
                entity(localId = "t1"),
                entity(localId = "t2", syncStatus = SyncStatus.PENDING_DELETE)
            )
        )

        val result = repository.getByAccountAndPeriod("local-a1", start, end).first()

        val success = result as DomainResult.Success
        assertThat(success.data.map { it.id }).containsExactly("t1")
    }

    @Test
    fun `getByAccountAndPeriod launches background pull for account`() = runTest {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T00:00:00Z")
        every { localDataSource.getByAccountAndPeriod(any(), any(), any()) } returns
            flowOf(emptyList())

        repository.getByAccountAndPeriod("local-a1", start, end)
        appCoroutineContext.runAll()

        coVerify(exactly = 1) {
            syncManager.pullFromRemoteForAccount(
                accountLocalId = "local-a1",
                startDate = ApiDateMapper.toApiDate(start),
                endDate = ApiDateMapper.toApiDate(end)
            )
        }
    }

    /* ---------- getById ---------- */

    @Test
    fun `getById returns local transaction immediately`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity()

        val result = repository.getById("local-t1")

        val success = result as DomainResult.Success
        assertThat(success.data.id).isEqualTo("local-t1")
        assertThat(success.data.amount).isEqualByComparingTo(BigDecimal("42.42"))
    }

    @Test
    fun `getById returns NoData when transaction is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.getById("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        assertThat(appCoroutineContext.launchCount).isZero()
    }

    @Test
    fun `getById propagates local db failure`() = runTest {
        val boom = IllegalStateException("db is down")
        coEvery { localDataSource.getByLocalId("local-t1") } throws boom

        val result = repository.getById("local-t1")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    @Test
    fun `getById refreshes synced transaction from server using category type`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { remoteDataSource.get("server-t1") } returns Response.success(
            TransactionDto(
                id = "server-t1",
                createdAt = createdAt,
                updatedAt = updatedAt,
                accountId = "server-a1",
                categoryId = "cat-income",
                amount = BigDecimal("99.00"),
                dateTime = transactionDate,
                comment = null
            )
        )
        coEvery { categoryLocalDataSource.getById("cat-income") } returns CategoryEntity(
            id = "cat-income",
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = "Salary",
            emoji = "💰",
            isIncome = true
        )
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        assertThat(updated.captured.localId).isEqualTo("local-t1")
        assertThat(updated.captured.type).isEqualTo(TransactionType.INCOME.name)
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
        assertThat(updated.captured.amount).isEqualTo("99.00")
    }

    @Test
    fun `getById skips refresh when category of server transaction is unknown`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { remoteDataSource.get("server-t1") } returns Response.success(
            TransactionDto(
                id = "server-t1",
                createdAt = createdAt,
                updatedAt = updatedAt,
                accountId = "server-a1",
                categoryId = "cat-unknown",
                amount = BigDecimal("99.00"),
                dateTime = transactionDate,
                comment = null
            )
        )
        coEvery { categoryLocalDataSource.getById("cat-unknown") } returns null

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `getById does not chase an unsynced transaction`() = runTest {
        // Создание такой транзакции уже стоит в очереди — догонять её отдельным запросом незачем
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = null)

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { remoteDataSource.get(any()) }
        coVerify(exactly = 0) { outboxEnqueuer.enqueue(any(), any(), any(), any(), any()) }
    }

    /* ---------- update ---------- */

    @Test
    fun `update returns NoData when transaction is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.update(transaction(id = "missing"))

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `update of synced transaction stores PENDING_UPDATE and enqueues update`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(transaction().copy(comment = "dinner"))

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.comment).isEqualTo("dinner")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "server-t1",
                body = any()
            )
        }
    }

    @Test
    fun `update of unsynced transaction addresses the operation by its client id`() = runTest {
        // serverId ещё нет, но сервер узнает транзакцию по localId, с которым ушло создание
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = null)
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(transaction())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "local-t1",
                body = any()
            )
        }
    }

    @Test
    fun `update replaces null comment with empty string`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        repository.update(transaction().copy(comment = null))

        assertThat(updated.captured.comment).isEmpty()
    }

    /* ---------- delete ---------- */

    @Test
    fun `delete marks transaction as PENDING_DELETE and enqueues delete`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.delete("local-t1")

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                operation = OutboxOperation.DELETE,
                targetServerId = "server-t1",
                body = null
            )
        }
    }

    @Test
    fun `delete returns NoData when transaction is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.delete("missing")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        assertThat(appCoroutineContext.launchCount).isZero()
    }

    /* ---------- hasTransactions ---------- */

    @Test
    fun `hasTransactions returns true when account has operations`() = runTest {
        coEvery { localDataSource.getByAccountId("local-a1") } returns listOf(entity())

        val result = repository.hasTransactions("local-a1")

        assertThat(result).isEqualTo(DomainResult.Success(true))
    }

    @Test
    fun `hasTransactions returns false when account has no operations`() = runTest {
        coEvery { localDataSource.getByAccountId("local-a1") } returns emptyList()

        val result = repository.hasTransactions("local-a1")

        assertThat(result).isEqualTo(DomainResult.Success(false))
    }

    @Test
    fun `hasTransactions returns Failure when local query throws`() = runTest {
        val boom = IllegalStateException("db")
        coEvery { localDataSource.getByAccountId("local-a1") } throws boom

        val result = repository.hasTransactions("local-a1")

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }
}
