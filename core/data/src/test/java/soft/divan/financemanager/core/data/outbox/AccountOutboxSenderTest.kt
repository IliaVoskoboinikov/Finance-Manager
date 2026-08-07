package soft.divan.financemanager.core.data.outbox

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.AccountStatus
import java.math.BigDecimal

/** Тесты [AccountOutboxSender] — включая архивную семантику удаления счёта. */
class AccountOutboxSenderTest {

    private val remoteDataSource = mockk<AccountRemoteDataSource>()
    private val localDataSource = mockk<AccountLocalDataSource>(relaxUnitFun = true)

    private val sender = AccountOutboxSender(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        gson = Gson()
    )

    private fun entry(
        operation: OutboxOperation = OutboxOperation.CREATE,
        targetServerId: String? = null
    ) = OutboxEntryEntity(
        sequenceNo = 1,
        entityType = OutboxEntityType.ACCOUNT,
        entityLocalId = "local-a1",
        operation = operation,
        targetServerId = targetServerId,
        payload = """{"id":"local-a1","name":"Cash","balance":"100.50","currencyId":"rub-id"}""",
        idempotencyKey = "local-a1",
        status = OutboxStatus.IN_PROGRESS,
        attemptCount = 0,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = 0,
        updatedAt = 0
    )

    private fun localEntity(status: String = AccountStatus.Active.name) = AccountEntity(
        localId = "local-a1",
        serverId = null,
        name = "Cash",
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.PENDING_CREATE,
        status = status
    )

    private fun dto() = AccountDto(
        id = "local-a1",
        userId = "u1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-02-01T00:00:00Z"
    )

    private fun <T> error(code: Int): Response<T> = Response.error(code, "".toResponseBody())

    @Test
    fun `successful create confirms the local row`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns Response.success(dto())
        coEvery { localDataSource.getByLocalId("local-a1") } returns localEntity()
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = sender.send(entry())

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        assertThat(updated.captured.serverId).isEqualTo("local-a1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `create succeeds when read-back finds the account after a lost ack`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns error(500)
        coEvery { remoteDataSource.getById("local-a1") } returns Response.success(dto())
        coEvery { localDataSource.getByLocalId("local-a1") } returns localEntity()

        val result = sender.send(entry())

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.update(any()) }
    }

    @Test
    fun `create is terminal when the server rejects the data`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns error(422)
        coEvery { remoteDataSource.getById("local-a1") } returns error(404)

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Terminal::class.java)
    }

    @Test
    fun `expired session blocks the entry without spending an attempt`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns error(401)
        coEvery { remoteDataSource.getById("local-a1") } returns error(401)

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Blocked::class.java)
    }

    @Test
    fun `successful delete removes an ordinary account locally`() = runTest {
        coEvery { remoteDataSource.delete("server-a1") } returns Response.success(Unit)
        coEvery { localDataSource.getByLocalId("local-a1") } returns localEntity()

        val result = sender.send(
            entry(operation = OutboxOperation.DELETE, targetServerId = "server-a1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.delete("local-a1") }
    }

    @Test
    fun `archived account survives deletion as a synced row`() = runTest {
        // Сервер перевёл счёт в архив — строка нужна истории операций, удалять её нельзя
        coEvery { remoteDataSource.delete("server-a1") } returns Response.success(Unit)
        coEvery { localDataSource.getByLocalId("local-a1") } returns
            localEntity(status = AccountStatus.Deleted.name)
        val updated = slot<AccountEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = sender.send(
            entry(operation = OutboxOperation.DELETE, targetServerId = "server-a1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 0) { localDataSource.delete(any()) }
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `delete treats 404 as an idempotent success`() = runTest {
        coEvery { remoteDataSource.delete("server-a1") } returns error(404)
        coEvery { localDataSource.getByLocalId("local-a1") } returns localEntity()

        val result = sender.send(
            entry(operation = OutboxOperation.DELETE, targetServerId = "server-a1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.delete("local-a1") }
    }
}
