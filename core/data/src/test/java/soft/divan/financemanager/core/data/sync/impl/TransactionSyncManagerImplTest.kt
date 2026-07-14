package soft.divan.financemanager.core.data.sync.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toUpdateDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.util.UUID

class TransactionSyncManagerImplTest {

    private val remoteDataSource = mockk<TransactionRemoteDataSource>()
    private val localDataSource = mockk<TransactionLocalDataSource>(relaxUnitFun = true)
    private val accountLocalDataSource = mockk<AccountLocalDataSource>()
    private val categoryLocalDataSource = mockk<CategoryLocalDataSource>()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val syncManager = TransactionSyncManagerImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        accountLocalDataSource = accountLocalDataSource,
        categoryLocalDataSource = categoryLocalDataSource,
        errorLogger = errorLogger
    )

    private fun accountEntity(serverId: String? = "server-a1") = AccountEntity(
        localId = "local-a1",
        serverId = serverId,
        name = "Cash",
        balance = "0",
        currencyId = "rub-id",
        createdAt = "2024-01-10T00:00:00Z",
        updatedAt = "2024-01-10T00:00:00Z",
        syncStatus = SyncStatus.SYNCED
    )

    private fun transactionDto(
        id: String = "server-t1",
        categoryId: String = "cat-1",
        updatedAt: String = "2024-02-01T00:00:00Z"
    ) = TransactionDto(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = updatedAt,
        accountId = "server-a1",
        categoryId = categoryId,
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    private fun transactionEntity(
        localId: String = "local-t1",
        serverId: String? = "server-t1",
        accountServerId: String? = "server-a1",
        updatedAt: String = "2024-01-15T00:00:00Z",
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = TransactionEntity(
        localId = localId,
        serverId = serverId,
        accountLocalId = "local-a1",
        type = TransactionType.EXPENSE.name,
        targetAccountLocalId = null,
        accountServerId = accountServerId,
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = "2024-01-15T10:00:00Z",
        comment = "lunch",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun categoryEntity(id: String = "cat-1", isIncome: Boolean = false) = CategoryEntity(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Food",
        emoji = "🍔",
        isIncome = isIncome
    )

    /* ---------- pullFromRemoteForAccount ---------- */

    @Test
    fun `pull inserts new server transaction with type from category`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto()))
        coEvery { localDataSource.getByServerIds(listOf("server-t1")) } returns emptyList()
        coEvery { categoryLocalDataSource.getById("cat-1") } returns
            categoryEntity(isIncome = true)
        val inserted = slot<TransactionEntity>()
        coEvery { localDataSource.insert(capture(inserted)) } returns Unit

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        assertThat(inserted.captured.serverId).isEqualTo("server-t1")
        assertThat(inserted.captured.accountLocalId).isEqualTo("local-a1")
        assertThat(inserted.captured.type).isEqualTo(TransactionType.INCOME.name)
        assertThat(inserted.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
        assertThat(UUID.fromString(inserted.captured.localId)).isNotNull()
    }

    @Test
    fun `pull updates local transaction when server version is newer`() = runTest {
        val local = transactionEntity(updatedAt = "2024-01-15T00:00:00Z")
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getByServerIds(listOf("server-t1")) } returns listOf(local)
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        assertThat(updated.captured.localId).isEqualTo("local-t1")
        assertThat(updated.captured.updatedAt).isEqualTo("2024-02-01T00:00:00Z")
    }

    @Test
    fun `pull keeps local transaction when server version is older`() = runTest {
        val local = transactionEntity(updatedAt = "2024-03-01T00:00:00Z")
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getByServerIds(listOf("server-t1")) } returns listOf(local)
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `pull skips transactions with unknown category`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(categoryId = "cat-unknown")))
        coEvery { localDataSource.getByServerIds(listOf("server-t1")) } returns emptyList()
        coEvery { categoryLocalDataSource.getById("cat-unknown") } returns null

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `pull does nothing when account is missing locally`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("missing") } returns null

        syncManager.pullFromRemoteForAccount("missing", "2024-01-01", "2024-01-31")

        coVerify(exactly = 0) { remoteDataSource.getByAccountAndPeriod(any(), any(), any()) }
    }

    @Test
    fun `pull does nothing when account has no server id`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns
            accountEntity(serverId = null)

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        coVerify(exactly = 0) { remoteDataSource.getByAccountAndPeriod(any(), any(), any()) }
    }

    /* ---------- pullServerData ---------- */

    @Test
    fun `pullServerData pulls transactions for every local account`() = runTest {
        coEvery { accountLocalDataSource.getAll() } returns flowOf(listOf(accountEntity()))
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", any(), any())
        } returns Response.success(emptyList())

        syncManager.pullServerData()

        // startDate = createdAt (2024-01-10) минус 2 дня
        coVerify(exactly = 1) {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-08", any())
        }
    }

    /* ---------- syncCreate ---------- */

    @Test
    fun `syncCreate pushes transaction and stores server id locally`() = runTest {
        val local = transactionEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
        coEvery { remoteDataSource.create(local.toDto("server-a1")) } returns
            Response.success(transactionDto())
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.syncCreate(local)

        assertThat(updated.captured.localId).isEqualTo("local-t1")
        assertThat(updated.captured.serverId).isEqualTo("server-t1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `syncCreate skips transactions without account server id`() = runTest {
        syncManager.syncCreate(transactionEntity(accountServerId = null))

        coVerify(exactly = 0) { remoteDataSource.create(any()) }
    }

    @Test
    fun `syncCreate does not touch local db when server call fails`() = runTest {
        coEvery { remoteDataSource.create(any()) } throws RuntimeException("offline")

        syncManager.syncCreate(transactionEntity(serverId = null))

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    /* ---------- syncUpdate ---------- */

    @Test
    fun `syncUpdate pushes update and marks local as SYNCED`() = runTest {
        val local = transactionEntity(syncStatus = SyncStatus.PENDING_UPDATE)
        coEvery {
            remoteDataSource.update("server-t1", local.toUpdateDto("server-a1"))
        } returns Response.success(Unit)
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.syncUpdate(local)

        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `syncUpdate skips transactions without server id`() = runTest {
        syncManager.syncUpdate(transactionEntity(serverId = null))

        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
    }

    @Test
    fun `syncUpdate skips transactions without account server id`() = runTest {
        syncManager.syncUpdate(transactionEntity(accountServerId = null))

        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
    }

    /* ---------- syncDelete ---------- */

    @Test
    fun `syncDelete removes unsynced transaction locally without server call`() = runTest {
        syncManager.syncDelete(transactionEntity(serverId = null))

        coVerify(exactly = 1) { localDataSource.delete("local-t1") }
        coVerify(exactly = 0) { remoteDataSource.delete(any()) }
    }

    @Test
    fun `syncDelete removes transaction on server then locally`() = runTest {
        coEvery { remoteDataSource.delete("server-t1") } returns Response.success(Unit)

        syncManager.syncDelete(transactionEntity())

        coVerify(exactly = 1) { remoteDataSource.delete("server-t1") }
        coVerify(exactly = 1) { localDataSource.delete("local-t1") }
    }

    @Test
    fun `syncDelete keeps local record when server delete fails`() = runTest {
        coEvery { remoteDataSource.delete("server-t1") } throws RuntimeException("offline")

        syncManager.syncDelete(transactionEntity())

        coVerify(exactly = 0) { localDataSource.delete(any()) }
    }

    /* ---------- syncWith ---------- */

    @Test
    fun `syncWith pulls server data and pushes pending changes`() = runTest {
        coEvery { accountLocalDataSource.getAll() } returns flowOf(emptyList())
        val pendingCreate = transactionEntity(
            localId = "local-c",
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        val pendingUpdate = transactionEntity(
            localId = "local-u",
            syncStatus = SyncStatus.PENDING_UPDATE
        )
        val pendingDelete = transactionEntity(
            localId = "local-d",
            serverId = null,
            syncStatus = SyncStatus.PENDING_DELETE
        )
        coEvery { localDataSource.getPendingSync() } returns
            listOf(pendingCreate, pendingUpdate, pendingDelete)
        coEvery { remoteDataSource.create(any()) } returns Response.success(transactionDto())
        coEvery { remoteDataSource.update(any(), any()) } returns Response.success(Unit)

        val result = syncManager.syncWith(object : Synchronizer {})

        assertThat(result).isTrue()
        coVerify(exactly = 1) { remoteDataSource.create(pendingCreate.toDto("server-a1")) }
        coVerify(exactly = 1) {
            remoteDataSource.update("server-t1", pendingUpdate.toUpdateDto("server-a1"))
        }
        coVerify(exactly = 1) { localDataSource.delete("local-d") }
    }
}
