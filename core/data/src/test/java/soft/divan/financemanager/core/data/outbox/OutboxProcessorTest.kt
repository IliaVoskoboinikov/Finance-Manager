package soft.divan.financemanager.core.data.outbox

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Тесты [OutboxProcessor] — механики очереди в отрыве от знания об эндпоинтах.
 *
 * Проверяется то, ради чего очередь и заводилась: строгий порядок, атомарный захват записи,
 * различение временных и терминальных ошибок, backoff и уход в dead-letter.
 */
class OutboxProcessorTest {

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val nowMillis = now.toEpochMilli()

    private val localDataSource = mockk<OutboxLocalDataSource>(relaxUnitFun = true)
    private val sender = mockk<OutboxSender>()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val processor = OutboxProcessor(
        localDataSource = localDataSource,
        sender = sender,
        retryPolicy = OutboxRetryPolicy(),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        errorLogger = errorLogger
    )

    private fun entry(
        sequenceNo: Long = 1L,
        entityLocalId: String = "T1",
        attemptCount: Int = 0
    ) = OutboxEntryEntity(
        sequenceNo = sequenceNo,
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = entityLocalId,
        operation = OutboxOperation.CREATE,
        targetServerId = null,
        payload = """{"id":"$entityLocalId"}""",
        idempotencyKey = entityLocalId,
        status = OutboxStatus.PENDING,
        attemptCount = attemptCount,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    private fun givenReady(vararg entries: OutboxEntryEntity, claimed: Boolean = true) {
        coEvery { localDataSource.getReadyToSend(any(), any()) } returns entries.toList()
        coEvery { localDataSource.markInProgress(any(), any()) } returns claimed
    }

    /* ---------- успешный путь ---------- */

    @Test
    fun `successful entry is marked completed`() = runTest {
        givenReady(entry(sequenceNo = 7))
        coEvery { sender.send(any()) } returns OutboxSendResult.Success

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 1) { localDataSource.markCompleted(7L, nowMillis) }
    }

    @Test
    fun `entries are sent in queue order`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T1")
        )
        val sent = mutableListOf<OutboxEntryEntity>()
        coEvery { sender.send(capture(sent)) } returns OutboxSendResult.Success

        processor.process()

        assertThat(sent.map { it.entityLocalId }).containsExactly("A1", "T1")
    }

    @Test
    fun `completed entries are cleaned up after a full drain`() = runTest {
        givenReady(entry())
        coEvery { sender.send(any()) } returns OutboxSendResult.Success

        processor.process()

        coVerify(exactly = 1) { localDataSource.deleteCompleted() }
    }

    /* ---------- захват записи ---------- */

    @Test
    fun `entry claimed by a parallel run is skipped`() = runTest {
        givenReady(entry(), claimed = false)

        processor.process()

        // Проигранный захват означает, что запись уже отправляет другой прогон
        coVerify(exactly = 0) { sender.send(any()) }
    }

    /* ---------- временные ошибки ---------- */

    @Test
    fun `transient failure schedules a retry with backoff and burns an attempt`() = runTest {
        givenReady(entry(sequenceNo = 3, attemptCount = 1))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")
        val nextAttemptAt = slot<Long>()
        coEvery {
            localDataSource.scheduleRetry(3L, 2, capture(nextAttemptAt), "HTTP 503", nowMillis)
        } returns Unit

        val drained = processor.process()

        assertThat(drained).isFalse()
        assertThat(nextAttemptAt.captured).isGreaterThan(nowMillis)
    }

    @Test
    fun `transient failure stops the run to preserve ordering`() = runTest {
        givenReady(entry(sequenceNo = 1), entry(sequenceNo = 2, entityLocalId = "T2"))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("timeout")

        processor.process()

        // Вторая запись может зависеть от первой — за неё не беремся
        coVerify(exactly = 1) { sender.send(any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(2L, any()) }
    }

    /* ---------- заблокированная сеть ---------- */

    @Test
    fun `blocked network requeues the entry without burning an attempt`() = runTest {
        givenReady(entry(sequenceNo = 5, attemptCount = 2))
        coEvery { sender.send(any()) } returns OutboxSendResult.Blocked("гостевой режим")

        val drained = processor.process()

        assertThat(drained).isFalse()
        // attemptCount не растёт: пользователь не виноват, что ещё не вошёл
        coVerify(exactly = 1) {
            localDataSource.scheduleRetry(5L, 2, 0L, "гостевой режим", nowMillis)
        }
        coVerify(exactly = 0) { localDataSource.markFailed(any(), any(), any(), any()) }
    }

    /* ---------- терминальные ошибки ---------- */

    @Test
    fun `terminal failure moves the entry to dead letter`() = runTest {
        givenReady(entry(sequenceNo = 9, attemptCount = 0))
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")

        processor.process()

        coVerify(exactly = 1) { localDataSource.markFailed(9L, 1, "HTTP 400", nowMillis) }
        coVerify(exactly = 0) { localDataSource.scheduleRetry(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `terminal failure does not block the rest of the queue`() = runTest {
        givenReady(entry(sequenceNo = 1), entry(sequenceNo = 2, entityLocalId = "T2"))
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Terminal("HTTP 422"),
            OutboxSendResult.Success
        )

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 1) { localDataSource.markCompleted(2L, nowMillis) }
    }

    @Test
    fun `exhausted attempts move the entry to dead letter instead of retrying forever`() = runTest {
        val lastAllowed = OutboxRetryPolicy.MAX_ATTEMPTS - 1
        givenReady(entry(sequenceNo = 4, attemptCount = lastAllowed))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 500")

        processor.process()

        coVerify(exactly = 0) { localDataSource.scheduleRetry(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) {
            localDataSource.markFailed(
                4L,
                OutboxRetryPolicy.MAX_ATTEMPTS,
                match { it.contains("Попытки исчерпаны") },
                nowMillis
            )
        }
    }

    /* ---------- пустая очередь ---------- */

    @Test
    fun `empty queue is a successful no-op`() = runTest {
        givenReady()

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 0) { sender.send(any()) }
    }
}
