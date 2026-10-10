package soft.divan.financemanager.core.data.outbox

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Тесты [OutboxEntryHandler] — судьбы **одной** записи после ответа сервера.
 *
 * Механика очереди (порядок, батчи, аренда) сюда не входит: она проверяется в
 * [OutboxProcessorTest]. Здесь только исход отправки и то, как он отражается в базе.
 */
class OutboxEntryHandlerTest {

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val nowMillis = now.toEpochMilli()

    private val localDataSource = mockk<OutboxLocalDataSource>(relaxUnitFun = true)
    private val sender = mockk<OutboxSender>()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    /** Пишет границы транзакции — по этому списку видно, что обе записи внутри одной. */
    private val transactionLog = mutableListOf<String>()

    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T {
            transactionLog += "begin"
            return try {
                block().also { transactionLog += "commit" }
            } catch (e: Throwable) {
                transactionLog += "rollback"
                throw e
            }
        }
    }

    private val handler = OutboxEntryHandler(
        localDataSource = localDataSource,
        sender = sender,
        retryPolicy = OutboxRetryPolicy(),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        errorLogger = errorLogger,
        transactionRunner = transactionRunner
    )

    private fun entry(
        sequenceNo: Long = 1L,
        entityLocalId: String = "T1",
        attemptCount: Int = 0,
        dependencyKey: String = entityLocalId
    ) = OutboxEntryEntity(
        sequenceNo = sequenceNo,
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = entityLocalId,
        dependencyKey = dependencyKey,
        operation = OutboxOperation.CREATE,
        targetServerId = null,
        payload = """{"id":"$entityLocalId"}""",
        idempotencyKey = "op-$entityLocalId",
        status = OutboxStatus.IN_PROGRESS,
        attemptCount = attemptCount,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    private fun givenCascadeStub() {
        coEvery { localDataSource.markFailed(any(), any(), any(), any(), any()) } returns 1
    }

    /* ---------- успех и атомарность обратного пути ---------- */

    @Test
    fun `successful entry is marked completed`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

        val outcome = handler.handle(entry(sequenceNo = 7))

        assertThat(outcome).isEqualTo(OutboxEntryOutcome.DONE)
        coVerify(exactly = 1) { localDataSource.markCompleted(7L, nowMillis) }
    }

    @Test
    fun `local effect and closing the entry happen in one transaction`() = runTest {
        val order = mutableListOf<String>()
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            localEffect = { order += "domain" }
        )
        coEvery { localDataSource.markCompleted(any(), any()) } answers { order += "outbox" }

        handler.handle(entry(sequenceNo = 7))

        assertThat(order).containsExactly("domain", "outbox")
        assertThat(transactionLog).containsExactly("begin", "commit")
    }

    @Test
    fun `a failing local effect rolls back closing the entry`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            localEffect = { error("сбой записи доменной строки") }
        )

        runCatching { handler.handle(entry(sequenceNo = 7)) }

        // Запись очереди не закрыта — операция уедет повторно, а не потеряется
        assertThat(transactionLog).containsExactly("begin", "rollback")
        coVerify(exactly = 0) { localDataSource.markCompleted(any(), any()) }
    }

    @Test
    fun `a failure while closing the entry rolls back the domain write`() = runTest {
        var domainApplied = false
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            localEffect = { domainApplied = true }
        )
        coEvery { localDataSource.markCompleted(any(), any()) } throws
            IllegalStateException("процесс убит между записями")

        runCatching { handler.handle(entry(sequenceNo = 7)) }

        // Эффект успел выполниться, но транзакция откатывается — рассогласования не остаётся
        assertThat(domainApplied).isTrue()
        assertThat(transactionLog).containsExactly("begin", "rollback")
    }

    @Test
    fun `success without a local effect still closes the entry`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(localEffect = null)

        handler.handle(entry(sequenceNo = 7))

        coVerify(exactly = 1) { localDataSource.markCompleted(7L, nowMillis) }
        assertThat(transactionLog).containsExactly("begin", "commit")
    }

    @Test
    fun `no transaction is opened for a failed send`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")

        handler.handle(entry(sequenceNo = 7))

        // Планирование повтора — одна запись, транзакция ей не нужна
        assertThat(transactionLog).isEmpty()
    }

    /* ---------- временные ошибки ---------- */

    @Test
    fun `transient failure schedules a retry with backoff and burns an attempt`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")
        val nextAttemptAt = slot<Long>()
        coEvery {
            localDataSource.scheduleRetry(3L, 2, capture(nextAttemptAt), "HTTP 503", nowMillis)
        } returns Unit

        val outcome = handler.handle(entry(sequenceNo = 3, attemptCount = 1))

        assertThat(outcome).isEqualTo(OutboxEntryOutcome.STALLED)
        assertThat(nextAttemptAt.captured).isGreaterThan(nowMillis)
    }

    /* ---------- заблокированная сеть ---------- */

    @Test
    fun `blocked network requeues the entry without burning an attempt`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Blocked("гостевой режим")

        val outcome = handler.handle(entry(sequenceNo = 5, attemptCount = 2))

        assertThat(outcome).isEqualTo(OutboxEntryOutcome.RUN_STALLED)
        // attemptCount не растёт: пользователь не виноват, что ещё не вошёл
        coVerify(exactly = 1) {
            localDataSource.scheduleRetry(5L, 2, 0L, "гостевой режим", nowMillis)
        }
        coVerify(exactly = 0) { localDataSource.markFailed(any(), any(), any(), any(), any()) }
    }

    /* ---------- терминальные ошибки и каскад ---------- */

    @Test
    fun `terminal failure moves the entry to dead letter`() = runTest {
        givenCascadeStub()
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")

        val outcome = handler.handle(entry(sequenceNo = 9))

        assertThat(outcome).isEqualTo(OutboxEntryOutcome.DONE)
        coVerify(exactly = 1) { localDataSource.markFailed(9L, "T1", 1, "HTTP 400", nowMillis) }
        coVerify(exactly = 0) { localDataSource.scheduleRetry(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `the cascade is keyed by the entity, not by its dependency group`() = runTest {
        givenCascadeStub()
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")

        // Транзакция T1 в группе счёта A1: отменить нужно ждущих ИМЕННО T1,
        // а не все операции счёта — ровесники T1 от неё не зависят
        handler.handle(entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"))

        coVerify(exactly = 1) {
            localDataSource.markFailed(
                sequenceNo = 1L,
                entityLocalId = "T1",
                attemptCount = 1,
                lastError = "HTTP 400",
                updatedAt = nowMillis
            )
        }
    }

    @Test
    fun `exhausted attempts move the entry to dead letter instead of retrying forever`() = runTest {
        givenCascadeStub()
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 500")

        val outcome = handler.handle(
            entry(sequenceNo = 4, attemptCount = OutboxRetryPolicy.MAX_ATTEMPTS - 1)
        )

        assertThat(outcome).isEqualTo(OutboxEntryOutcome.DONE)
        coVerify(exactly = 0) { localDataSource.scheduleRetry(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) {
            localDataSource.markFailed(
                4L,
                "T1",
                OutboxRetryPolicy.MAX_ATTEMPTS,
                match { it.contains("Попытки исчерпаны") },
                nowMillis
            )
        }
    }

    @Test
    fun `a retryable failure does not cancel anything`() = runTest {
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")

        handler.handle(entry(sequenceNo = 1))

        // Операция ещё жива — отменять зависящие от неё рано
        coVerify(exactly = 0) { localDataSource.markFailed(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `dead letter is reported without the operation payload`() = runTest {
        givenCascadeStub()
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")
        val message = slot<String>()
        coEvery { errorLogger.recordError(capture(message)) } returns Unit

        handler.handle(entry(sequenceNo = 9, entityLocalId = "T1"))

        // В содержимом операции лежат суммы — в отчёт об ошибке они попадать не должны
        assertThat(message.captured).contains("T1", "HTTP 400")
        assertThat(message.captured).doesNotContain("payload", "amount")
    }
}
