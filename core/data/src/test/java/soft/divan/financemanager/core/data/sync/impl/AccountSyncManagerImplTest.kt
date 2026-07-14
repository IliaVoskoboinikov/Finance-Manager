package soft.divan.financemanager.core.data.sync.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toUpdateDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.util.UUID

/**
 * Тесты [AccountSyncManagerImpl] — двусторонней синхронизации счетов.
 *
 * Проверяются обе фазы: pull (server → local, разрешение конфликтов по `updatedAt`,
 * last-write-wins) и push (local → server по `syncStatus`: PENDING_CREATE/UPDATE/DELETE),
 * а также устойчивость к сетевым ошибкам (при провале API локальная запись не трогается).
 */
class AccountSyncManagerImplTest {

    private val remoteDataSource = mockk<AccountRemoteDataSource>()
    private val localDataSource = mockk<AccountLocalDataSource>(relaxUnitFun = true)
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val syncManager = AccountSyncManagerImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        errorLogger = errorLogger
    )

    private val synchronizer = object : Synchronizer {}

    private fun dto(id: String = "server-1", updatedAt: String = "2024-02-01T00:00:00Z") = AccountDto(
        id = id,
        userId = "u1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = updatedAt
    )

    private fun entity(
        localId: String = "local-1",
        serverId: String? = "server-1",
        updatedAt: String = "2024-01-15T00:00:00Z",
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    /* ---------- pullServerData ---------- */

    @Test
    fun `pull creates local account for unknown server account`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(listOf(dto()))
        coEvery { localDataSource.getByServerIds(listOf("server-1")) } returns emptyList()
        val created = slot<AccountEntity>()
        coEvery { localDataSource.create(capture(created)) } returns Unit

        syncManager.pullServerData()

        assertThat(created.captured.serverId).isEqualTo("server-1")
        assertThat(created.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
        // localId генерируется заново и должен быть валидным UUID
        assertThat(UUID.fromString(created.captured.localId)).isNotNull()
    }

    @Test
    fun `pull updates local account when server version is newer`() = runTest {
        val local = entity(updatedAt = "2024-01-15T00:00:00Z")
        coEvery { remoteDataSource.getAll() } returns
            Response.success(listOf(dto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getByServerIds(listOf("server-1")) } returns listOf(local)
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.pullServerData()

        assertThat(updated.captured.localId).isEqualTo("local-1")
        assertThat(updated.captured.updatedAt).isEqualTo("2024-02-01T00:00:00Z")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `pull keeps local account when server version is older`() = runTest {
        val local = entity(updatedAt = "2024-03-01T00:00:00Z")
        coEvery { remoteDataSource.getAll() } returns
            Response.success(listOf(dto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getByServerIds(listOf("server-1")) } returns listOf(local)

        syncManager.pullServerData()

        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) { localDataSource.create(any()) }
    }

    @Test
    fun `pull does nothing when server call fails`() = runTest {
        coEvery { remoteDataSource.getAll() } throws RuntimeException("offline")

        syncManager.pullServerData()

        coVerify(exactly = 0) { localDataSource.getByServerIds(any()) }
        coVerify(exactly = 0) { localDataSource.create(any()) }
    }

    /* ---------- syncCreate ---------- */

    @Test
    fun `syncCreate updates local account with server data on success`() = runTest {
        val request = entity(serverId = null).toDto()
        coEvery { remoteDataSource.create(request) } returns Response.success(dto())
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.syncCreate(accountDto = request, localId = "local-1")

        assertThat(updated.captured.localId).isEqualTo("local-1")
        assertThat(updated.captured.serverId).isEqualTo("server-1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `syncCreate does not touch local db when server call fails`() = runTest {
        coEvery { remoteDataSource.create(any()) } throws RuntimeException("offline")

        syncManager.syncCreate(accountDto = entity(serverId = null).toDto(), localId = "local-1")

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    /* ---------- syncUpdate ---------- */

    @Test
    fun `syncUpdate pushes update and marks local as SYNCED`() = runTest {
        val local = entity(syncStatus = SyncStatus.PENDING_UPDATE)
        coEvery { remoteDataSource.update("server-1", local.toUpdateDto()) } returns
            Response.success(Unit)
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        syncManager.syncUpdate(local)

        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `syncUpdate skips accounts without server id`() = runTest {
        syncManager.syncUpdate(entity(serverId = null))

        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `syncUpdate keeps local status when server call fails`() = runTest {
        coEvery { remoteDataSource.update(any(), any()) } throws RuntimeException("offline")

        syncManager.syncUpdate(entity(syncStatus = SyncStatus.PENDING_UPDATE))

        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    /* ---------- syncDelete ---------- */

    @Test
    fun `syncDelete removes unsynced account locally without server call`() = runTest {
        syncManager.syncDelete(entity(serverId = null))

        coVerify(exactly = 1) { localDataSource.delete("local-1") }
        coVerify(exactly = 0) { remoteDataSource.delete(any()) }
    }

    @Test
    fun `syncDelete removes account on server then locally`() = runTest {
        coEvery { remoteDataSource.delete("server-1") } returns Response.success(Unit)

        syncManager.syncDelete(entity())

        coVerify(exactly = 1) { remoteDataSource.delete("server-1") }
        coVerify(exactly = 1) { localDataSource.delete("local-1") }
    }

    @Test
    fun `syncDelete keeps local record when server delete fails`() = runTest {
        coEvery { remoteDataSource.delete("server-1") } throws RuntimeException("offline")

        syncManager.syncDelete(entity())

        coVerify(exactly = 0) { localDataSource.delete(any()) }
    }

    /* ---------- syncWith ---------- */

    @Test
    fun `syncWith pulls server data and pushes pending changes`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(emptyList())
        val pendingCreate = entity(
            localId = "local-c",
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        val pendingUpdate = entity(localId = "local-u", syncStatus = SyncStatus.PENDING_UPDATE)
        val pendingDelete = entity(
            localId = "local-d",
            serverId = null,
            syncStatus = SyncStatus.PENDING_DELETE
        )
        coEvery { localDataSource.getPendingSync() } returns
            listOf(pendingCreate, pendingUpdate, pendingDelete)
        coEvery { remoteDataSource.create(any()) } returns Response.success(dto())
        coEvery { remoteDataSource.update(any(), any()) } returns Response.success(Unit)

        val result = syncManager.syncWith(synchronizer)

        assertThat(result).isTrue()
        coVerify(exactly = 1) { remoteDataSource.create(pendingCreate.toDto()) }
        coVerify(exactly = 1) { remoteDataSource.update("server-1", pendingUpdate.toUpdateDto()) }
        coVerify(exactly = 1) { localDataSource.delete("local-d") }
    }

    @Test
    fun `syncWith ignores already synced accounts during push`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(emptyList())
        coEvery { localDataSource.getPendingSync() } returns listOf(entity())

        val result = syncManager.syncWith(synchronizer)

        assertThat(result).isTrue()
        coVerify(exactly = 0) { remoteDataSource.create(any()) }
        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
        coVerify(exactly = 0) { localDataSource.delete(any()) }
    }
}
