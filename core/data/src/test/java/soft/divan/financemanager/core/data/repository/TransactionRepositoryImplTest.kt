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
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.transaction.TransactionRollbackException
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.outbox.OutboxEnqueuer
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
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

    /** По умолчанию в очереди нет операций над транзакцией. */
    private val outboxLocalDataSource = mockk<OutboxLocalDataSource> {
        coEvery { hasOpenOperationsAffecting(any()) } returns false
    }

    /**
     * Выполняет блок как есть, но, как и настоящий runner, превращает откат в `Failure`:
     * сама атомарность проверяется отдельно, на реальном Room.
     */
    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = try {
            block()
        } catch (e: TransactionRollbackException) {
            @Suppress("UNCHECKED_CAST")
            DomainResult.Failure(e.error) as T
        }
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
        outboxLocalDataSource = outboxLocalDataSource,
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
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = transactionDate,
        comment = "lunch",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun accountEntity(
        localId: String = "local-a1",
        serverId: String? = "server-a1"
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = "Cash",
        balance = "0",
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.SYNCED
    )

    /** Счёт операции по умолчанию синхронизирован; тесты с другим счётом переопределяют стаб. */
    @Before
    fun stubAccount() {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery { accountLocalDataSource.getByServerId("server-a1") } returns accountEntity()
    }

    /* ---------- create ---------- */

    @Test
    fun `create stores PENDING_CREATE entity without server id`() = runTest {
        val saved = slot<TransactionEntity>()
        coEvery { localDataSource.insert(capture(saved)) } returns Unit

        val result = repository.create(transaction())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(saved.captured.localId).isEqualTo("local-t1")
        assertThat(saved.captured.serverId).isNull()
        assertThat(saved.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
    }

    @Test
    fun `create enqueues an outbox create addressed to the account server id`() = runTest {
        coEvery { localDataSource.insert(any()) } returns Unit
        val body = slot<Any>()

        repository.create(transaction())

        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                // Транзакция входит в группу своего счёта — порядок с ним обязателен
                dependencyKey = "local-a1",
                operation = OutboxOperation.CREATE,
                targetServerId = null,
                body = capture(body)
            )
        }
        assertThat((body.captured as TransactionRequestDto).accountId).isEqualTo("server-a1")
    }

    @Test
    fun `create on an account without serverId sends the account's client id`() = runTest {
        // Гость или офлайн: счёт ещё не на сервере, но барьер порядка выпустит операцию только
        // после POST счёта, а сервер создаст его под этим localId
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns
            accountEntity(serverId = null)
        coEvery { localDataSource.insert(any()) } returns Unit
        val body = slot<Any>()

        val result = repository.create(transaction())

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        coVerify {
            outboxEnqueuer.enqueue(
                entityType = any(),
                entityLocalId = any(),
                dependencyKey = "local-a1",
                operation = OutboxOperation.CREATE,
                targetServerId = any(),
                body = capture(body)
            )
        }
        assertThat((body.captured as TransactionRequestDto).accountId).isEqualTo("local-a1")
    }

    @Test
    fun `create returns Failure and enqueues nothing when the account cannot be read`() = runTest {
        val boom = IllegalStateException("db closed")
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } throws boom

        val result = repository.create(transaction())

        // Сбой чтения — не «счёта нет»: закрытая база не должна выглядеть как NoData
        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        coVerify(exactly = 0) { localDataSource.insert(any()) }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `create returns NoData when the account is missing`() = runTest {
        // Без строки счёта POST ушёл бы с неизвестным серверу accountId и осел бы в dead-letter
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns null

        val result = repository.create(transaction())

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        coVerify(exactly = 0) { localDataSource.insert(any()) }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `create returns Failure when local insert throws`() = runTest {
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
        // updatedAt у строки и ответа равны: refresh даты не сравнивает, важно лишь, что строка
        // не менялась за время запроса
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
    fun `getById moves the transaction to the account of the server version`() = runTest {
        // Другое устройство перенесло операцию на второй счёт: без этого следующая правка
        // ушла бы со старым счётом и вернула перенос на сервере
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { remoteDataSource.get("server-t1") } returns Response.success(
            serverTransaction(accountId = "server-a2")
        )
        coEvery { categoryLocalDataSource.getById("cat-1") } returns foodCategory()
        coEvery { accountLocalDataSource.getByServerId("server-a2") } returns
            accountEntity(localId = "local-a2", serverId = "server-a2").copy(currencyId = "usd-id")
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        assertThat(updated.captured.accountLocalId).isEqualTo("local-a2")
        // Валюта операции — валюта её счёта, как и при pull
        assertThat(updated.captured.currencyId).isEqualTo("usd-id")
    }

    @Test
    fun `getById skips refresh when account of server transaction is unknown`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { remoteDataSource.get("server-t1") } returns Response.success(
            serverTransaction(accountId = "server-unknown")
        )
        coEvery { categoryLocalDataSource.getById("cat-1") } returns foodCategory()
        coEvery { accountLocalDataSource.getByServerId("server-unknown") } returns null

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    private fun serverTransaction(accountId: String) = TransactionDto(
        id = "server-t1",
        createdAt = createdAt,
        updatedAt = updatedAt,
        accountId = accountId,
        categoryId = "cat-1",
        amount = BigDecimal("99.00"),
        dateTime = transactionDate,
        comment = null
    )

    private fun foodCategory() = CategoryEntity(
        id = "cat-1",
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    @Test
    fun `getById does not take a server version older than the row written during the refresh`() =
        runTest {
            val beforeRequest = entity(serverId = "server-t1").copy(updatedAt = createdAt)
            coEvery { localDataSource.getByLocalId("local-t1") } returnsMany listOf(
                beforeRequest, // getById
                beforeRequest, // проверка до запроса
                entity(serverId = "server-t1") // pull успел записать версию от updatedAt
            )
            // Ответ новее строки на момент запроса, но старше версии, которую записал pull
            coEvery { remoteDataSource.get("server-t1") } returns Response.success(
                serverTransaction(accountId = "server-a1").copy(updatedAt = transactionDate)
            )
            coEvery { categoryLocalDataSource.getById("cat-1") } returns foodCategory()
            coEvery { accountLocalDataSource.getByServerId("server-a1") } returns accountEntity()

            repository.getById("local-t1")
            appCoroutineContext.runAll()

            coVerify(exactly = 0) { localDataSource.update(any()) }
        }

    @Test
    fun `getById takes a server version newer than the local row`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns
            entity(serverId = "server-t1").copy(updatedAt = createdAt)
        coEvery { remoteDataSource.get("server-t1") } returns Response.success(
            serverTransaction(accountId = "server-a1")
        )
        coEvery { categoryLocalDataSource.getById("cat-1") } returns foodCategory()
        coEvery { accountLocalDataSource.getByServerId("server-a1") } returns accountEntity()
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        assertThat(updated.captured.amount).isEqualTo("99.00")
        assertThat(updated.captured.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `getById does not refresh a transaction with a pending operation`() = runTest {
        // Строка ждёт отправки удаления: ответ сервера вернул бы её в списки
        coEvery { localDataSource.getByLocalId("local-t1") } returns
            entity(serverId = "server-t1", syncStatus = SyncStatus.PENDING_DELETE)

        repository.getById("local-t1")
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { remoteDataSource.get(any()) }
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `getById does not overwrite a transaction queued for sending during the refresh`() =
        runTest {
            coEvery { localDataSource.getByLocalId("local-t1") } returns
                entity(serverId = "server-t1")
            coEvery { remoteDataSource.get("server-t1") } returns Response.success(
                TransactionDto(
                    id = "server-t1",
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    accountId = "server-a1",
                    categoryId = "cat-1",
                    amount = BigDecimal("99.00"),
                    dateTime = transactionDate,
                    comment = null
                )
            )
            coEvery { categoryLocalDataSource.getById("cat-1") } returns CategoryEntity(
                id = "cat-1",
                createdAt = createdAt,
                updatedAt = updatedAt,
                name = "Food",
                emoji = "🍔",
                isIncome = false
            )
            // До запроса очередь пуста, к моменту ответа в ней появилась правка
            coEvery { outboxLocalDataSource.hasOpenOperationsAffecting("local-t1") } returnsMany
                listOf(false, true)

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
                // Транзакция входит в группу своего счёта — порядок с ним обязателен
                dependencyKey = "local-a1",
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
                // Транзакция входит в группу своего счёта — порядок с ним обязателен
                dependencyKey = "local-a1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "local-t1",
                body = any()
            )
        }
    }

    @Test
    fun `update moving to another account rewrites the account and waits for it`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns
            accountEntity(localId = "local-a2", serverId = "server-a2")
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit
        val body = slot<Any>()

        val result = repository.update(
            transaction().copy(accountLocalId = "local-a2", type = TransactionType.INCOME)
        )

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.accountLocalId).isEqualTo("local-a2")
        assertThat(updated.captured.type).isEqualTo(TransactionType.INCOME.name)
        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                // Правка уходит после создания нового счёта, а не старого
                dependencyKey = "local-a2",
                operation = OutboxOperation.UPDATE,
                targetServerId = "server-t1",
                body = capture(body),
                // Пока перенос не уехал, сервер держит операцию на старом счёте
                formerDependencyKey = "local-a1"
            )
        }
        assertThat((body.captured as UpdateTransactionRequestDto).accountId).isEqualTo("server-a2")
    }

    @Test
    fun `update moving to an unsynced account addresses it by its client id`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns
            accountEntity(localId = "local-a2", serverId = null)
        coEvery { localDataSource.update(any()) } returns Unit
        val body = slot<Any>()

        repository.update(transaction().copy(accountLocalId = "local-a2"))

        coVerify {
            outboxEnqueuer.enqueue(
                entityType = any(),
                entityLocalId = any(),
                dependencyKey = "local-a2",
                operation = any(),
                targetServerId = any(),
                body = capture(body),
                formerDependencyKey = "local-a1"
            )
        }
        // Сервер узнает счёт по localId, с которым ушло его создание
        assertThat((body.captured as UpdateTransactionRequestDto).accountId).isEqualTo("local-a2")
    }

    @Test
    fun `update without moving sends the account server id it has now`() = runTest {
        // Операция создана, пока счёт был без serverId; с тех пор он синхронизировался. Правка
        // должна адресовать счёт по его текущему serverId, а не по снимку на момент создания
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns
            accountEntity(serverId = "server-a1-new")
        coEvery { localDataSource.update(any()) } returns Unit
        val body = slot<Any>()

        repository.update(transaction())

        coVerify(exactly = 1) {
            outboxEnqueuer.enqueue(
                entityType = any(),
                entityLocalId = any(),
                dependencyKey = "local-a1",
                operation = any(),
                targetServerId = any(),
                body = capture(body),
                formerDependencyKey = null
            )
        }
        assertThat((body.captured as UpdateTransactionRequestDto).accountId)
            .isEqualTo("server-a1-new")
    }

    @Test
    fun `update returns Failure when the new account cannot be read`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        val boom = IllegalStateException("db closed")
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } throws boom

        val result = repository.update(transaction().copy(accountLocalId = "local-a2"))

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `update returns NoData when the new account is missing`() = runTest {
        // Без строки счёта PUT ушёл бы с неизвестным серверу accountId и осел бы в dead-letter
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns null

        val result = repository.update(transaction().copy(accountLocalId = "local-a2"))

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `update without moving returns NoData when the account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("local-t1") } returns entity(serverId = "server-t1")
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns null

        val result = repository.update(transaction())

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) {
            outboxEnqueuer.enqueue(any(), any(), any(), any(), any(), any(), any())
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
                // Транзакция входит в группу своего счёта — порядок с ним обязателен
                dependencyKey = "local-a1",
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
