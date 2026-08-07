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
import soft.divan.financemanager.core.auth.data.interceptor.GuestModeNetworkBlockedException
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import java.math.BigDecimal
import java.net.SocketTimeoutException

/**
 * Тесты [TransactionOutboxSender]: перевод HTTP-исхода в исход очереди и отражение результата
 * в локальной БД.
 */
class TransactionOutboxSenderTest {

    private val remoteDataSource = mockk<TransactionRemoteDataSource>()
    private val localDataSource = mockk<TransactionLocalDataSource>(relaxUnitFun = true)

    private val sender = TransactionOutboxSender(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        gson = Gson()
    )

    private fun entry(
        operation: OutboxOperation = OutboxOperation.CREATE,
        targetServerId: String? = null
    ) = OutboxEntryEntity(
        sequenceNo = 1,
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = "local-t1",
        operation = operation,
        targetServerId = targetServerId,
        payload = """{"id":"local-t1","accountId":"local-a1","categoryId":"cat-1",""" +
            """"amount":"42.42","dateTime":"2024-01-15T10:00:00Z","comment":"lunch"}""",
        idempotencyKey = "local-t1",
        status = OutboxStatus.IN_PROGRESS,
        attemptCount = 0,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = 0,
        updatedAt = 0
    )

    private fun localEntity() = TransactionEntity(
        localId = "local-t1",
        serverId = null,
        accountLocalId = "local-a1",
        type = TransactionType.EXPENSE.name,
        targetAccountLocalId = null,
        accountServerId = "local-a1",
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = "2024-01-15T10:00:00Z",
        comment = "lunch",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.PENDING_CREATE
    )

    private fun dto() = TransactionDto(
        id = "local-t1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-02-01T00:00:00Z",
        accountId = "local-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    private fun <T> error(code: Int): Response<T> = Response.error(code, "".toResponseBody())

    /* ---------- create ---------- */

    @Test
    fun `successful create confirms the local row`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns Response.success(dto())
        coEvery { localDataSource.getByLocalId("local-t1") } returns localEntity()
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = sender.send(entry())

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        assertThat(updated.captured.serverId).isEqualTo("local-t1")
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
        assertThat(updated.captured.updatedAt).isEqualTo("2024-02-01T00:00:00Z")
    }

    @Test
    fun `create sends the snapshot payload as the request body`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns Response.success(dto())
        coEvery { localDataSource.getByLocalId(any()) } returns localEntity()
        val request = slot<soft.divan.financemanager.core.data.dto.TransactionRequestDto>()
        coEvery { remoteDataSource.create(capture(request)) } returns Response.success(dto())

        sender.send(entry())

        assertThat(request.captured.id).isEqualTo("local-t1")
        assertThat(request.captured.comment).isEqualTo("lunch")
        assertThat(request.captured.amount).isEqualByComparingTo(BigDecimal("42.42"))
    }

    @Test
    fun `create succeeds when read-back finds the transaction after a lost ack`() = runTest {
        // Сервер применил POST, но ответ не дошёл: повтор отвергнут, а чтение находит запись
        coEvery { remoteDataSource.create(any()) } returns error(500)
        coEvery { remoteDataSource.get("local-t1") } returns Response.success(dto())
        coEvery { localDataSource.getByLocalId("local-t1") } returns localEntity()

        val result = sender.send(entry())

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.update(any()) }
    }

    @Test
    fun `create stays transient when read-back does not find the transaction`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns error(503)
        coEvery { remoteDataSource.get("local-t1") } returns error(404)

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Transient::class.java)
        coVerify(exactly = 0) { localDataSource.update(any()) }
    }

    @Test
    fun `create is terminal when the server rejects the data`() = runTest {
        coEvery { remoteDataSource.create(any()) } returns error(400)
        coEvery { remoteDataSource.get("local-t1") } returns error(404)

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Terminal::class.java)
    }

    @Test
    fun `blocked network skips the read-back entirely`() = runTest {
        coEvery { remoteDataSource.create(any()) } throws GuestModeNetworkBlockedException()

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Blocked::class.java)
        // Перепроверять нечего: запрос не дошёл до сервера
        coVerify(exactly = 0) { remoteDataSource.get(any()) }
    }

    @Test
    fun `network failure is transient`() = runTest {
        coEvery { remoteDataSource.create(any()) } throws SocketTimeoutException("timeout")
        coEvery { remoteDataSource.get(any()) } throws SocketTimeoutException("timeout")

        val result = sender.send(entry())

        assertThat(result).isInstanceOf(OutboxSendResult.Transient::class.java)
    }

    /* ---------- update ---------- */

    @Test
    fun `successful update marks the local row synced`() = runTest {
        coEvery { remoteDataSource.update("server-t1", any()) } returns Response.success(Unit)
        coEvery { localDataSource.getByLocalId("local-t1") } returns localEntity()
        val updated = slot<TransactionEntity>()
        coEvery { localDataSource.update(capture(updated)) } returns Unit

        val result = sender.send(
            entry(operation = OutboxOperation.UPDATE, targetServerId = "server-t1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        assertThat(updated.captured.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `update without a target id is terminal`() = runTest {
        val result = sender.send(entry(operation = OutboxOperation.UPDATE, targetServerId = null))

        assertThat(result).isInstanceOf(OutboxSendResult.Terminal::class.java)
        coVerify(exactly = 0) { remoteDataSource.update(any(), any()) }
    }

    /* ---------- delete ---------- */

    @Test
    fun `successful delete removes the local row`() = runTest {
        coEvery { remoteDataSource.delete("server-t1") } returns Response.success(Unit)

        val result = sender.send(
            entry(operation = OutboxOperation.DELETE, targetServerId = "server-t1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.delete("local-t1") }
    }

    @Test
    fun `delete treats 404 as an idempotent success`() = runTest {
        // Записи на сервере уже нет — цель удаления достигнута
        coEvery { remoteDataSource.delete("server-t1") } returns error(404)

        val result = sender.send(
            entry(operation = OutboxOperation.DELETE, targetServerId = "server-t1")
        )

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 1) { localDataSource.delete("local-t1") }
    }

    @Test
    fun `delete of a never-synced transaction skips the network`() = runTest {
        val result = sender.send(entry(operation = OutboxOperation.DELETE, targetServerId = null))

        assertThat(result).isEqualTo(OutboxSendResult.Success)
        coVerify(exactly = 0) { remoteDataSource.delete(any()) }
        coVerify(exactly = 1) { localDataSource.delete("local-t1") }
    }
}
