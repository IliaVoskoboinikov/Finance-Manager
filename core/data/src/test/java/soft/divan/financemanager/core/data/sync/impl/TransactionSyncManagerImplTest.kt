package soft.divan.financemanager.core.data.sync.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.Synchronizer
import soft.divan.financemanager.core.data.testing.PassThroughDataGuard
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

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
        dataGuard = PassThroughDataGuard,
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
        updatedAt: String = "2024-01-15T00:00:00Z",
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
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns emptyList()
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
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns listOf(local)
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
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns listOf(local)
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
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns emptyList()
        coEvery { categoryLocalDataSource.getById("cat-unknown") } returns null

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `pull reports failure instead of throwing when a category cannot be read`() = runTest {
        // База закрылась посреди слияния (авто-блокировка): исключение оборвало бы pull
        // остальных счетов в pullServerData, а сбой должен остаться сбоем этого счёта
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto()))
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns emptyList()
        coEvery { categoryLocalDataSource.getById("cat-1") } throws IllegalStateException("closed")

        val result = syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        assertThat(result).isFalse()
        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `pull does nothing when account is missing locally`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("missing") } returns null

        val result = syncManager.pullFromRemoteForAccount("missing", "2024-01-01", "2024-01-31")

        assertThat(result).isFalse()
        coVerify(exactly = 0) { remoteDataSource.getByAccountAndPeriod(any(), any(), any()) }
    }

    @Test
    fun `pull does nothing when account has no server id`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns
            accountEntity(serverId = null)

        val result = syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        // Не сбой: тянуть нечего, создание счёта ещё в очереди
        assertThat(result).isTrue()
        coVerify(exactly = 0) { remoteDataSource.getByAccountAndPeriod(any(), any(), any()) }
    }

    @Test
    fun `pull stops when local transactions cannot be read`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", any(), any())
        } returns Response.success(listOf(transactionDto()))
        coEvery { localDataSource.getBySyncIds(any()) } throws
            IllegalStateException("too many SQL variables")

        val result = syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        // Иначе каждая серверная транзакция выглядела бы новой и вставлялась бы дубликатом
        assertThat(result).isFalse()
        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `pull reports failure when the server call fails`() = runTest {
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", any(), any())
        } throws SocketTimeoutException("offline")

        val result = syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        assertThat(result).isFalse()
    }

    /* ---------- pullServerData ---------- */

    @Test
    fun `pullServerData pulls every account even after one of them fails`() = runTest {
        val second = accountEntity().copy(localId = "local-a2", serverId = "server-a2")
        coEvery { accountLocalDataSource.getAll() } returns flowOf(listOf(accountEntity(), second))
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns second
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", any(), any())
        } throws SocketTimeoutException("offline")
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a2", any(), any())
        } returns Response.success(emptyList())
        coEvery { localDataSource.getBySyncIds(emptyList()) } returns emptyList()

        val result = syncManager.pullServerData()

        assertThat(result).isFalse()
        coVerify(exactly = 1) { remoteDataSource.getByAccountAndPeriod("server-a2", any(), any()) }
    }

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

    @Test
    fun `pull does not duplicate locally created transaction awaiting confirmation`() = runTest {
        // Потеря ACK: сервер создал транзакцию с нашим клиентским id, локальная ещё PENDING_CREATE
        // (serverId == null), поэтому по serverId она не находится — но это та же сущность.
        val pending = transactionEntity(
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(id = "local-t1")))
        coEvery { localDataSource.getBySyncIds(listOf("local-t1")) } returns listOf(pending)
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        // Дубликат не создаётся: запись опознана по тому же localId.
        // Подтвердит её отправитель очереди — pull в чужую незавершённую операцию не лезет.
        coVerify(exactly = 0) { localDataSource.insert(any()) }
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `pull does not overwrite a transaction with unsent local changes`() = runTest {
        // Пользователь поправил сумму, операция ещё в очереди; серверная версия её не знает
        val edited = transactionEntity(
            syncStatus = SyncStatus.PENDING_UPDATE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns listOf(edited)
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        // Иначе правка откатилась бы на глазах у пользователя до отправки из очереди
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `pull does not resurrect a transaction awaiting deletion`() = runTest {
        val deleted = transactionEntity(
            syncStatus = SyncStatus.PENDING_DELETE,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(updatedAt = "2024-02-01T00:00:00Z")))
        coEvery { localDataSource.getBySyncIds(listOf("server-t1")) } returns listOf(deleted)
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")

        // Перезапись вернула бы транзакцию в списки: PENDING_DELETE скрывает её, SYNCED — нет
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    /* ---------- параллельные pull ---------- */

    /**
     * Локальная таблица транзакций поверх моков: чтение и вставка с задержкой, чтобы
     * параллельные pull успели переплестись между «прочитал» и «вставил». `safeDbCall` уводит
     * чтение и вставку на IO-диспатчер: `delay` там идёт в реальном, а не виртуальном времени —
     * на этом держится воспроизведение гонки, — а вставки приходят из разных потоков, отсюда
     * потокобезопасный список.
     */
    private fun backLocalTableWithList(): MutableList<TransactionEntity> {
        val table = CopyOnWriteArrayList<TransactionEntity>()
        coEvery { localDataSource.getBySyncIds(any()) } coAnswers {
            val ids = firstArg<List<String>>()
            delay(DB_DELAY_MS)
            table.filter { it.serverId in ids }
        }
        coEvery { localDataSource.insert(any()) } coAnswers {
            delay(DB_DELAY_MS)
            table += firstArg<TransactionEntity>()
        }
        return table
    }

    @Test
    fun `parallel pulls of one account insert a server transaction once`() = runTest {
        // Экраны «Сегодня», «История» и SyncWorker тянут один счёт одновременно
        val table = backLocalTableWithList()
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", any(), any())
        } returns Response.success(listOf(transactionDto()))
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        coroutineScope {
            launch { syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31") }
            launch { syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31") }
        }

        // Второй pull обязан увидеть строку, вставленную первым, а не вставить свою
        assertThat(table.map { it.serverId }).containsExactly("server-t1")
    }

    @Test
    fun `parallel pull for another period is not skipped`() = runTest {
        // Периоды у вызовов разные: пропуск второго, как у pull счетов, потерял бы его данные
        val table = backLocalTableWithList()
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns Response.success(listOf(transactionDto(id = "server-jan")))
        coEvery {
            remoteDataSource.getByAccountAndPeriod("server-a1", "2024-02-01", "2024-02-29")
        } returns Response.success(listOf(transactionDto(id = "server-feb")))
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        coroutineScope {
            launch { syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31") }
            launch { syncManager.pullFromRemoteForAccount("local-a1", "2024-02-01", "2024-02-29") }
        }

        assertThat(table.map { it.serverId }).containsExactlyInAnyOrder("server-jan", "server-feb")
    }

    @Test
    fun `parallel pulls of two accounts insert a moved transaction once`() = runTest {
        // Операцию перенесли на другой счёт на другом устройстве: её возвращают pull'ы обоих
        // счетов, поэтому лок общий — с локом на счёт она легла бы двумя строками
        val table = backLocalTableWithList()
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns
            accountEntity().copy(localId = "local-a2", serverId = "server-a2")
        coEvery { remoteDataSource.getByAccountAndPeriod(any(), any(), any()) } returns
            Response.success(listOf(transactionDto(id = "server-moved")))
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        coroutineScope {
            launch { syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31") }
            launch { syncManager.pullFromRemoteForAccount("local-a2", "2024-01-01", "2024-01-31") }
        }

        assertThat(table.map { it.serverId }).containsExactly("server-moved")
    }

    @Test
    fun `slow server response of one account does not hold merges of others`() = runTest {
        // Лок общий на все счета, поэтому сеть обязана идти вне его: иначе зависший запрос
        // одного счёта (SyncWorker) держал бы свежие данные всех остальных экранов
        val table = backLocalTableWithList()
        val slowRequestStarted = CompletableDeferred<Unit>()
        val releaseSlowResponse = CompletableDeferred<Unit>()
        coEvery { accountLocalDataSource.getByLocalId("local-a1") } returns accountEntity()
        coEvery { accountLocalDataSource.getByLocalId("local-a2") } returns
            accountEntity().copy(localId = "local-a2", serverId = "server-a2")
        coEvery { remoteDataSource.getByAccountAndPeriod("server-a1", any(), any()) } coAnswers {
            slowRequestStarted.complete(Unit)
            releaseSlowResponse.await()
            Response.success(listOf(transactionDto(id = "server-slow")))
        }
        coEvery { remoteDataSource.getByAccountAndPeriod("server-a2", any(), any()) } returns
            Response.success(listOf(transactionDto(id = "server-fast")))
        coEvery { categoryLocalDataSource.getById("cat-1") } returns categoryEntity()

        val slowPull = launch {
            syncManager.pullFromRemoteForAccount("local-a1", "2024-01-01", "2024-01-31")
        }
        slowRequestStarted.await()
        // С локом вокруг сети этот вызов повис бы до таймаута runTest
        syncManager.pullFromRemoteForAccount("local-a2", "2024-01-01", "2024-01-31")

        assertThat(table.map { it.serverId }).containsExactly("server-fast")
        releaseSlowResponse.complete(Unit)
        slowPull.join()
        assertThat(table.map { it.serverId }).containsExactly("server-fast", "server-slow")
    }

    /* ---------- syncWith ---------- */

    @Test
    fun `syncWith only pulls server data`() = runTest {
        // Отправку локальных изменений выполняет очередь исходящих операций, а не менеджер
        coEvery { accountLocalDataSource.getAll() } returns flowOf(emptyList())

        val result = syncManager.syncWith(object : Synchronizer {})

        assertThat(result).isTrue()
        coVerify(exactly = 0) { remoteDataSource.create(any(), any()) }
        coVerify(exactly = 0) { remoteDataSource.update(any(), any(), any()) }
        coVerify(exactly = 0) { remoteDataSource.delete(any(), any()) }
    }

    private companion object {
        const val DB_DELAY_MS = 50L
    }
}
