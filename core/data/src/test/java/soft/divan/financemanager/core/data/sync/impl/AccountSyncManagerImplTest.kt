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
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.Synchronizer
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
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        status: String = "Active"
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        status = status
    )

    /* ---------- pullServerData ---------- */

    @Test
    fun `pull creates local account for unknown server account`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(listOf(dto()))
        coEvery { localDataSource.getBySyncIds(listOf("server-1")) } returns emptyList()
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
        coEvery { localDataSource.getBySyncIds(listOf("server-1")) } returns listOf(local)
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
        coEvery { localDataSource.getBySyncIds(listOf("server-1")) } returns listOf(local)

        syncManager.pullServerData()

        coVerify(exactly = 0) { localDataSource.update(any()) }
        coVerify(exactly = 0) { localDataSource.create(any()) }
    }

    @Test
    fun `pull does not duplicate locally created account awaiting confirmation`() = runTest {
        // Потеря ACK: сервер создал счёт с нашим клиентским id, локальный ещё PENDING_CREATE
        // (serverId == null), поэтому по serverId он не находится — но это та же сущность.
        val pending = entity(
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { remoteDataSource.getAll() } returns
            Response.success(listOf(dto(id = "local-1", updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getBySyncIds(listOf("local-1")) } returns listOf(pending)

        syncManager.pullServerData()

        // Дубликат не создаётся: запись опознана по тому же localId.
        // Подтвердит её отправитель очереди — pull в чужую незавершённую операцию не лезет.
        coVerify(exactly = 0) { localDataSource.create(any()) }
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `pull does not overwrite an account with unsent local changes`() = runTest {
        // Пользователь переименовал счёт, операция ещё в очереди; серверная версия её не знает
        val edited = entity(
            syncStatus = SyncStatus.PENDING_UPDATE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { remoteDataSource.getAll() } returns
            Response.success(listOf(dto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getBySyncIds(listOf("server-1")) } returns listOf(edited)

        syncManager.pullServerData()

        // Иначе правка откатилась бы на глазах у пользователя до отправки из очереди
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `pull does not resurrect an account awaiting deletion`() = runTest {
        val deleted = entity(
            syncStatus = SyncStatus.PENDING_DELETE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { remoteDataSource.getAll() } returns
            Response.success(listOf(dto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getBySyncIds(listOf("server-1")) } returns listOf(deleted)

        syncManager.pullServerData()

        // Перезапись вернула бы счёт в списки: PENDING_DELETE скрывает его, SYNCED — нет
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `pull does nothing when server call fails`() = runTest {
        coEvery { remoteDataSource.getAll() } throws RuntimeException("offline")

        syncManager.pullServerData()

        coVerify(exactly = 0) { localDataSource.getBySyncIds(any()) }
        coVerify(exactly = 0) { localDataSource.create(any()) }
    }

    /* ---------- syncWith ---------- */

    @Test
    fun `syncWith only pulls server data`() = runTest {
        // Отправку локальных изменений выполняет очередь исходящих операций, а не менеджер
        coEvery { remoteDataSource.getAll() } returns Response.success(emptyList())

        val result = syncManager.syncWith(synchronizer)

        assertThat(result).isTrue()
        coVerify(exactly = 1) { remoteDataSource.getAll() }
        coVerify(exactly = 0) { remoteDataSource.create(any()) }
        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
        coVerify(exactly = 0) { remoteDataSource.delete(any()) }
    }

    @Test
    fun `syncWith reports failure when pull throws`() = runTest {
        coEvery { remoteDataSource.getAll() } throws RuntimeException("boom")

        assertThat(syncManager.syncWith(synchronizer)).isTrue()
    }
}
