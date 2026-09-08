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

    /** Пишет, что происходило внутри транзакции, — по этому списку видно её границы. */
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

    private val processor = OutboxProcessor(
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
        // По умолчанию каждая запись независима — так проверяется механика без влияния групп
        dependencyKey: String = entityLocalId
    ) = OutboxEntryEntity(
        sequenceNo = sequenceNo,
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = entityLocalId,
        dependencyKey = dependencyKey,
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
        coEvery { localDataSource.getReadyToSend(any(), any(), any()) } returns entries.toList()
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns claimed
        // Возвращает число затронутых строк, поэтому relaxUnitFun его не покрывает
        coEvery { localDataSource.markFailed(any(), any(), any(), any(), any()) } returns 1
    }

    /* ---------- успешный путь ---------- */

    @Test
    fun `successful entry is marked completed`() = runTest {
        givenReady(entry(sequenceNo = 7))
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

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
        coEvery { sender.send(capture(sent)) } returns OutboxSendResult.Success()

        processor.process()

        assertThat(sent.map { it.entityLocalId }).containsExactly("A1", "T1")
    }

    @Test
    fun `completed entries are cleaned up after a full drain`() = runTest {
        givenReady(entry())
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

        processor.process()

        coVerify(exactly = 1) { localDataSource.deleteCompleted() }
    }

    /* ---------- дренаж в несколько проходов ---------- */

    /** Каждый проход получает свою выборку — так моделируется разблокировка барьером. */
    private fun givenPasses(vararg passes: List<OutboxEntryEntity>) {
        coEvery {
            localDataSource.getReadyToSend(any(), any(), any())
        } returnsMany passes.toList()
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns true
        coEvery { localDataSource.markFailed(any(), any(), any(), any(), any()) } returns 1
    }

    @Test
    fun `a second pass picks up what the first one unblocked`() = runTest {
        // Барьер не выдаёт правку, пока открыто создание, — она появляется только во втором проходе
        givenPasses(
            listOf(entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1")),
            listOf(entry(sequenceNo = 2, entityLocalId = "T1", dependencyKey = "A1")),
            emptyList()
        )
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

        val drained = processor.process()

        // Без повторного прохода правка ждала бы ближайшего фонового синка
        assertThat(drained).isTrue()
        coVerify(exactly = 2) { sender.send(any()) }
    }

    @Test
    fun `no extra pass is made when nothing was closed`() = runTest {
        givenPasses(listOf(entry(sequenceNo = 1)))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")

        processor.process()

        // Прогресса нет — повторять выборку незачем
        coVerify(exactly = 1) { localDataSource.getReadyToSend(any(), any(), any()) }
    }

    @Test
    fun `an empty queue costs exactly one query`() = runTest {
        givenPasses(emptyList())

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 1) { localDataSource.getReadyToSend(any(), any(), any()) }
    }

    @Test
    fun `a stalled pass keeps the run reported as not drained`() = runTest {
        givenPasses(
            listOf(
                entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
                entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2")
            ),
            emptyList()
        )
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Transient("HTTP 503"),
            OutboxSendResult.Success()
        )

        val drained = processor.process()

        // Пустой второй проход не должен «затирать» застревание в первом
        assertThat(drained).isFalse()
    }

    @Test
    fun `an entry is never handled twice within one run`() = runTest {
        // Выборка упорно отдаёт одну и ту же запись — повторно брать её нельзя
        coEvery {
            localDataSource.getReadyToSend(any(), any(), any())
        } returns listOf(entry(sequenceNo = 1))
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns true
        coEvery { localDataSource.markFailed(any(), any(), any(), any(), any()) } returns 1
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

        processor.process()

        coVerify(exactly = 1) { sender.send(any()) }
    }

    @Test
    fun `passes are capped so an endless supply cannot loop forever`() = runTest {
        // Патологический случай: каждая выборка отдаёт новую запись, и та успешно закрывается
        var nextSequenceNo = 0L
        coEvery { localDataSource.getReadyToSend(any(), any(), any()) } answers {
            listOf(entry(sequenceNo = ++nextSequenceNo))
        }
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns true
        coEvery { localDataSource.markFailed(any(), any(), any(), any(), any()) } returns 1
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()

        processor.process()

        // Разбор обязан завершиться: остальное подберёт следующий прогон
        coVerify(exactly = OutboxProcessor.MAX_PASSES) {
            localDataSource.getReadyToSend(any(), any(), any())
        }
    }

    /* ---------- атомарность обратного пути ---------- */

    @Test
    fun `local effect and closing the entry happen in one transaction`() = runTest {
        givenReady(entry(sequenceNo = 7))
        val order = mutableListOf<String>()
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            OutboxLocalEffect { order += "domain" }
        )
        coEvery { localDataSource.markCompleted(any(), any()) } answers { order += "outbox" }

        processor.process()

        // Обе записи внутри одной транзакции: сначала домен, затем закрытие операции
        assertThat(order).containsExactly("domain", "outbox")
        assertThat(transactionLog).containsExactly("begin", "commit")
    }

    @Test
    fun `a failing local effect rolls back closing the entry`() = runTest {
        givenReady(entry(sequenceNo = 7))
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            OutboxLocalEffect { error("сбой записи доменной строки") }
        )

        runCatching { processor.process() }

        // Запись очереди не закрыта — операция уедет повторно, а не потеряется
        assertThat(transactionLog).containsExactly("begin", "rollback")
        coVerify(exactly = 0) { localDataSource.markCompleted(any(), any()) }
    }

    @Test
    fun `a failure while closing the entry rolls back the domain write`() = runTest {
        givenReady(entry(sequenceNo = 7))
        var domainApplied = false
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(
            OutboxLocalEffect { domainApplied = true }
        )
        coEvery { localDataSource.markCompleted(any(), any()) } throws
            IllegalStateException("процесс убит между записями")

        runCatching { processor.process() }

        // Эффект успел выполниться, но транзакция откатывается — рассогласования не остаётся
        assertThat(domainApplied).isTrue()
        assertThat(transactionLog).containsExactly("begin", "rollback")
    }

    @Test
    fun `success without a local effect still closes the entry`() = runTest {
        givenReady(entry(sequenceNo = 7))
        coEvery { sender.send(any()) } returns OutboxSendResult.Success(localEffect = null)

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 1) { localDataSource.markCompleted(7L, nowMillis) }
        assertThat(transactionLog).containsExactly("begin", "commit")
    }

    @Test
    fun `no transaction is opened for a failed send`() = runTest {
        givenReady(entry(sequenceNo = 7))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")

        processor.process()

        // Планирование повтора — одна запись, транзакция ей не нужна
        assertThat(transactionLog).isEmpty()
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
    fun `transient failure stalls only the dependent group`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A1"),
            entry(sequenceNo = 3, entityLocalId = "T3", dependencyKey = "A1")
        )
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("timeout")

        val drained = processor.process()

        // Остальные операции группы зависят от неуехавшей — за них не беремся
        assertThat(drained).isFalse()
        coVerify(exactly = 1) { sender.send(any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(2L, any(), any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(3L, any(), any()) }
    }

    @Test
    fun `transient failure does not stall independent groups`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2"),
            entry(sequenceNo = 3, entityLocalId = "T3", dependencyKey = "A3")
        )
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Transient("timeout"),
            OutboxSendResult.Success(),
            OutboxSendResult.Success()
        )

        val drained = processor.process()

        // Ровно то, о чём говорил ревьюер: сбой одной операции не держит независимые
        assertThat(drained).isFalse()
        coVerify(exactly = 3) { sender.send(any()) }
        coVerify(exactly = 1) { localDataSource.markCompleted(2L, nowMillis) }
        coVerify(exactly = 1) { localDataSource.markCompleted(3L, nowMillis) }
    }

    @Test
    fun `a stalled group skips all its later operations in one run`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2"),
            entry(sequenceNo = 3, entityLocalId = "T3", dependencyKey = "A1")
        )
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Transient("timeout"),
            OutboxSendResult.Success()
        )

        processor.process()

        // seq 3 из застрявшей группы пропущена, хотя барьер в БД её и не отфильтровал
        coVerify(exactly = 2) { sender.send(any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(3L, any(), any()) }
    }

    /* ---------- каскад dead-letter ---------- */

    @Test
    fun `terminal failure cancels the operations depending on it`() = runTest {
        givenReady(entry(sequenceNo = 1, entityLocalId = "A1", dependencyKey = "A1"))
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")

        processor.process()

        // Без каскада зависимые операции потратили бы все попытки и всё равно умерли бы.
        // Группа передаётся в тот же запрос — отказ головы и отмена зависимых атомарны.
        coVerify(exactly = 1) {
            localDataSource.markFailed(
                sequenceNo = 1L,
                dependencyKey = "A1",
                attemptCount = 1,
                lastError = "HTTP 400",
                updatedAt = nowMillis
            )
        }
    }

    @Test
    fun `exhausted attempts also cancel the dependent operations`() = runTest {
        val lastAllowed = OutboxRetryPolicy.MAX_ATTEMPTS - 1
        givenReady(
            entry(
                sequenceNo = 4,
                entityLocalId = "A1",
                attemptCount = lastAllowed,
                dependencyKey = "A1"
            )
        )
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 500")

        processor.process()

        coVerify(exactly = 1) {
            localDataSource.markFailed(4L, "A1", OutboxRetryPolicy.MAX_ATTEMPTS, any(), nowMillis)
        }
    }

    @Test
    fun `a retryable failure does not cancel anything`() = runTest {
        givenReady(entry(sequenceNo = 1, attemptCount = 0))
        coEvery { sender.send(any()) } returns OutboxSendResult.Transient("HTTP 503")

        processor.process()

        // Операция ещё жива — отменять зависящие от неё рано
        coVerify(exactly = 0) { localDataSource.markFailed(any(), any(), any(), any(), any()) }
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
        coVerify(exactly = 0) { localDataSource.markFailed(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `blocked network stops the whole run, independent groups included`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2")
        )
        coEvery { sender.send(any()) } returns OutboxSendResult.Blocked("гостевой режим")

        processor.process()

        // Это состояние клиента, а не сервера: независимые группы упрутся в ту же стену
        coVerify(exactly = 1) { sender.send(any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(2L, any(), any()) }
    }

    /* ---------- терминальные ошибки ---------- */

    @Test
    fun `terminal failure moves the entry to dead letter`() = runTest {
        givenReady(entry(sequenceNo = 9, attemptCount = 0))
        coEvery { sender.send(any()) } returns OutboxSendResult.Terminal("HTTP 400")

        processor.process()

        coVerify(exactly = 1) { localDataSource.markFailed(9L, "T1", 1, "HTTP 400", nowMillis) }
        coVerify(exactly = 0) { localDataSource.scheduleRetry(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `terminal failure does not block the rest of the queue`() = runTest {
        givenReady(entry(sequenceNo = 1), entry(sequenceNo = 2, entityLocalId = "T2"))
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Terminal("HTTP 422"),
            OutboxSendResult.Success()
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
                "T1",
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

    @Test
    fun `sent entries are cleaned up even when the run stops early`() = runTest {
        givenReady(entry(sequenceNo = 1L), entry(sequenceNo = 2L, entityLocalId = "T2"))
        coEvery { sender.send(any()) } returnsMany listOf(
            OutboxSendResult.Success(),
            OutboxSendResult.Transient("HTTP 503")
        )

        processor.process()

        // Первая запись отправлена — она не должна ждать удаления до следующего удачного прогона
        coVerify(exactly = 1) { localDataSource.deleteCompleted() }
    }

    /* ---------- аренда ---------- */

    @Test
    fun `entries taken into work are leased for a bounded time`() = runTest {
        givenReady(entry())
        coEvery { sender.send(any()) } returns OutboxSendResult.Success()
        val staleBefore = slot<Long>()
        coEvery {
            localDataSource.getReadyToSend(any(), capture(staleBefore), any())
        } returns listOf(entry())

        processor.process()

        // Порог «брошенности» отстоит от текущего момента, а не совпадает с ним: запись,
        // взятую только что, чужой прогон забрать не сможет
        assertThat(staleBefore.captured).isLessThan(nowMillis)
        coVerify(exactly = 1) {
            localDataSource.markInProgress(1L, staleBefore.captured, nowMillis)
        }
    }
}
