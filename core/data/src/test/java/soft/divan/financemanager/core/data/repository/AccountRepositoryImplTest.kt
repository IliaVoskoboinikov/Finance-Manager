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
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
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
    private val appCoroutineContext = RecordingAppCoroutineContext()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val repository = AccountRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        transactionLocalDataSource = transactionLocalDataSource,
        syncManager = syncManager,
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
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
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
    fun `create launches background sync with dto and local id`() = runTest {
        coEvery { localDataSource.create(any()) } returns Unit

        repository.create(account())
        appCoroutineContext.runAll()

        coVerify(exactly = 1) {
            syncManager.syncCreate(accountDto = account().toDto(), localId = "local-1")
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
    fun `getById pushes unsynced account to server in background`() = runTest {
        val local = entity(serverId = null)
        coEvery { localDataSource.getByLocalId("local-1") } returns local

        repository.getById("local-1")
        appCoroutineContext.runAll()

        coVerify(exactly = 1) {
            syncManager.syncCreate(accountDto = local.toDto(), localId = "local-1")
        }
    }

    /* ---------- update ---------- */

    @Test
    fun `update returns NoData when account is missing`() = runTest {
        coEvery { localDataSource.getByLocalId("missing") } returns null

        val result = repository.update(account(id = "missing"))

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `update of synced account stores PENDING_UPDATE and syncs update`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = "server-1")
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(account().copy(name = "Renamed"))
        appCoroutineContext.runAll()

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.name).isEqualTo("Renamed")
        assertThat(updated.captured.serverId).isEqualTo("server-1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
        coVerify(exactly = 1) {
            syncManager.syncUpdate(
                match { it.serverId == "server-1" && it.syncStatus == SyncStatus.PENDING_UPDATE }
            )
        }
    }

    @Test
    fun `update of unsynced account stores PENDING_CREATE and syncs create`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity(serverId = null)
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.update(account())
        appCoroutineContext.runAll()

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_CREATE)
        coVerify(exactly = 1) {
            syncManager.syncCreate(accountDto = account().toDto(), localId = "local-1")
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
    fun `delete marks account as PENDING_DELETE and syncs delete`() = runTest {
        val local = entity(serverId = "server-1")
        coEvery { localDataSource.getByLocalId("local-1") } returns local
        coEvery { transactionLocalDataSource.getByAccountId("local-1") } returns emptyList()
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = repository.delete("local-1")
        appCoroutineContext.runAll()

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        coVerify(exactly = 1) { syncManager.syncDelete(local) }
    }

    @Test
    fun `delete fails when account has transactions`() = runTest {
        coEvery { localDataSource.getByLocalId("local-1") } returns entity()
        coEvery { transactionLocalDataSource.getByAccountId("local-1") } returns
            listOf(mockk<TransactionEntity>())

        val result = repository.delete("local-1")

        val failure = result as DomainResult.Failure
        assertThat(failure.error).isInstanceOf(DomainError.Unknown::class.java)
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
