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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Тесты [OutboxProcessor] — механики очереди: какие записи берутся, в каком порядке, сколько
 * проходов и когда прогон останавливается.
 *
 * Судьба отдельной записи (подтвердить / повторить / dead-letter) сюда не входит — она проверяется
 * в [OutboxEntryHandlerTest], а здесь обработчик замокан и отвечает готовым исходом.
 */
class OutboxProcessorTest {

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val nowMillis = now.toEpochMilli()

    private val localDataSource = mockk<OutboxLocalDataSource>(relaxUnitFun = true)
    private val entryHandler = mockk<OutboxEntryHandler>()

    private val processor = OutboxProcessor(
        localDataSource = localDataSource,
        entryHandler = entryHandler,
        clock = Clock.fixed(now, ZoneOffset.UTC)
    )

    private fun entry(
        sequenceNo: Long = 1L,
        entityLocalId: String = "T1",
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
        status = OutboxStatus.PENDING,
        attemptCount = 0,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    private fun givenReady(vararg entries: OutboxEntryEntity, claimed: Boolean = true) {
        coEvery { localDataSource.getReadyToSend(any(), any(), any()) } returns entries.toList()
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns claimed
    }

    /** Каждый проход получает свою выборку — так моделируется разблокировка барьером. */
    private fun givenPasses(vararg passes: List<OutboxEntryEntity>) {
        coEvery { localDataSource.getReadyToSend(any(), any(), any()) } returnsMany passes.toList()
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns true
    }

    private fun givenOutcome(vararg outcomes: OutboxEntryOutcome) {
        coEvery { entryHandler.handle(any()) } returnsMany outcomes.toList()
    }

    /* ---------- обычный проход ---------- */

    @Test
    fun `entries are sent in queue order`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T1")
        )
        val sent = mutableListOf<OutboxEntryEntity>()
        coEvery { entryHandler.handle(capture(sent)) } returns OutboxEntryOutcome.DONE

        processor.process()

        assertThat(sent.map { it.entityLocalId }).startsWith("A1", "T1")
    }

    @Test
    fun `empty queue is a successful no-op`() = runTest {
        givenReady()

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 0) { entryHandler.handle(any()) }
    }

    @Test
    fun `completed entries are cleaned up after a full drain`() = runTest {
        givenPasses(listOf(entry()), emptyList())
        givenOutcome(OutboxEntryOutcome.DONE)

        processor.process()

        coVerify(exactly = 1) { localDataSource.deleteCompleted() }
    }

    @Test
    fun `sent entries are cleaned up even when the run stops early`() = runTest {
        givenReady(entry(sequenceNo = 1), entry(sequenceNo = 2, entityLocalId = "T2"))
        givenOutcome(OutboxEntryOutcome.DONE, OutboxEntryOutcome.STALLED)

        processor.process()

        // Первая запись отправлена — она не должна ждать удаления до следующего удачного прогона
        coVerify(exactly = 1) { localDataSource.deleteCompleted() }
    }

    /* ---------- захват записи и аренда ---------- */

    @Test
    fun `entry claimed by a parallel run is skipped`() = runTest {
        givenReady(entry(), claimed = false)

        processor.process()

        // Проигранный захват означает, что запись уже отправляет другой прогон
        coVerify(exactly = 0) { entryHandler.handle(any()) }
    }

    @Test
    fun `entries taken into work are leased for a bounded time`() = runTest {
        givenReady(entry())
        givenOutcome(OutboxEntryOutcome.DONE)
        val staleBefore = slot<Long>()
        coEvery {
            localDataSource.getReadyToSend(any(), capture(staleBefore), any())
        } returns listOf(entry())

        processor.process()

        // Порог «брошенности» отстоит от текущего момента, а не совпадает с ним: запись,
        // взятую только что, чужой прогон забрать не сможет
        assertThat(staleBefore.captured).isLessThan(nowMillis)
        coVerify(atLeast = 1) {
            localDataSource.markInProgress(1L, staleBefore.captured, nowMillis)
        }
    }

    /* ---------- застревание: что пропускается, а что нет ---------- */

    @Test
    fun `a stalled entry blocks the operations that depend on it`() = runTest {
        // Создание T1 застряло; правка T1 (та же строка) в этом проходе идти не должна
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T1", dependencyKey = "A1")
        )
        givenOutcome(OutboxEntryOutcome.STALLED)

        val drained = processor.process()

        assertThat(drained).isFalse()
        coVerify(exactly = 1) { entryHandler.handle(any()) }
    }

    @Test
    fun `a stalled account blocks its transactions`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "A1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T1", dependencyKey = "A1")
        )
        givenOutcome(OutboxEntryOutcome.STALLED)

        processor.process()

        // Транзакция назвала A1 предшественником — поверх неуехавшего счёта её слать нельзя
        coVerify(exactly = 1) { entryHandler.handle(any()) }
    }

    @Test
    fun `a stalled transaction does not block its siblings`() = runTest {
        // Ровесники: обе транзакции ждут счёт A1, но друг от друга не зависят
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A1"),
            entry(sequenceNo = 3, entityLocalId = "T3", dependencyKey = "A1")
        )
        givenOutcome(
            OutboxEntryOutcome.STALLED,
            OutboxEntryOutcome.DONE,
            OutboxEntryOutcome.DONE
        )

        val drained = processor.process()

        assertThat(drained).isFalse()
        coVerify(exactly = 3) { entryHandler.handle(any()) }
    }

    @Test
    fun `a stalled transaction blocks the operations on its account`() = runTest {
        // Обратное ребро: удаление счёта нельзя слать раньше неуехавшей транзакции этого счёта
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "A1", dependencyKey = "A1")
        )
        givenOutcome(OutboxEntryOutcome.STALLED)

        processor.process()

        coVerify(exactly = 1) { entryHandler.handle(any()) }
    }

    @Test
    fun `an unrelated entry is not blocked by a stalled one`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2")
        )
        givenOutcome(OutboxEntryOutcome.STALLED, OutboxEntryOutcome.DONE)

        processor.process()

        coVerify(exactly = 2) { entryHandler.handle(any()) }
    }

    @Test
    fun `a blocked network stops the whole run`() = runTest {
        givenReady(
            entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
            entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A2")
        )
        givenOutcome(OutboxEntryOutcome.RUN_STALLED)

        val drained = processor.process()

        // Это состояние клиента, а не сервера: независимые операции упрутся в ту же стену
        assertThat(drained).isFalse()
        coVerify(exactly = 1) { entryHandler.handle(any()) }
        coVerify(exactly = 0) { localDataSource.markInProgress(2L, any(), any()) }
    }

    @Test
    fun `a closed entry does not block anything`() = runTest {
        givenPasses(
            listOf(
                entry(sequenceNo = 1, entityLocalId = "T1", dependencyKey = "A1"),
                entry(sequenceNo = 2, entityLocalId = "T2", dependencyKey = "A1")
            ),
            emptyList()
        )
        givenOutcome(OutboxEntryOutcome.DONE, OutboxEntryOutcome.DONE)

        val drained = processor.process()

        assertThat(drained).isTrue()
        coVerify(exactly = 2) { entryHandler.handle(any()) }
    }

    /* ---------- дренаж в несколько проходов ---------- */

    @Test
    fun `a second pass picks up what the first one unblocked`() = runTest {
        givenPasses(
            listOf(entry(sequenceNo = 1, entityLocalId = "A1", dependencyKey = "A1")),
            listOf(entry(sequenceNo = 2, entityLocalId = "T1", dependencyKey = "A1")),
            emptyList()
        )
        givenOutcome(OutboxEntryOutcome.DONE, OutboxEntryOutcome.DONE)

        val drained = processor.process()

        // Без повторного прохода транзакция ждала бы ближайшего фонового синка
        assertThat(drained).isTrue()
        coVerify(exactly = 2) { entryHandler.handle(any()) }
    }

    @Test
    fun `no extra pass is made when nothing was closed`() = runTest {
        givenPasses(listOf(entry(sequenceNo = 1)))
        givenOutcome(OutboxEntryOutcome.STALLED)

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
        givenOutcome(OutboxEntryOutcome.STALLED, OutboxEntryOutcome.DONE)

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
        coEvery { entryHandler.handle(any()) } returns OutboxEntryOutcome.DONE

        processor.process()

        coVerify(exactly = 1) { entryHandler.handle(any()) }
    }

    @Test
    fun `passes are capped so an endless supply cannot loop forever`() = runTest {
        var nextSequenceNo = 0L
        coEvery { localDataSource.getReadyToSend(any(), any(), any()) } answers {
            listOf(entry(sequenceNo = ++nextSequenceNo))
        }
        coEvery { localDataSource.markInProgress(any(), any(), any()) } returns true
        coEvery { entryHandler.handle(any()) } returns OutboxEntryOutcome.DONE

        processor.process()

        // Разбор обязан завершиться: остальное подберёт следующий прогон
        coVerify(exactly = OutboxProcessor.MAX_PASSES) {
            localDataSource.getReadyToSend(any(), any(), any())
        }
    }
}
