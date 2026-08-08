package soft.divan.financemanager.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus

/**
 * Тесты [OutboxDao] на реальном in-memory Room: проверяются настоящие SQL-запросы —
 * FIFO-порядок выдачи, учёт backoff (`nextAttemptAt`) и переходы статусов очереди.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutboxDaoTest : RoomDaoTest() {

    private val dao get() = db.outboxDao()

    private val now = 1_000_000L

    private fun entry(
        entityLocalId: String = "T1",
        entityType: OutboxEntityType = OutboxEntityType.TRANSACTION,
        operation: OutboxOperation = OutboxOperation.CREATE,
        targetServerId: String? = null,
        status: OutboxStatus = OutboxStatus.PENDING,
        attemptCount: Int = 0,
        nextAttemptAt: Long = 0,
        lastError: String? = null
    ) = OutboxEntryEntity(
        entityType = entityType,
        entityLocalId = entityLocalId,
        operation = operation,
        targetServerId = targetServerId,
        payload = """{"id":"$entityLocalId"}""",
        idempotencyKey = entityLocalId,
        status = status,
        attemptCount = attemptCount,
        nextAttemptAt = nextAttemptAt,
        lastError = lastError,
        createdAt = now,
        updatedAt = now
    )

    /* ---------- insert & порядок ---------- */

    @Test
    fun `insert assigns increasing sequence numbers`() = runTest {
        val first = dao.insert(entry("T1"))
        val second = dao.insert(entry("T2"))

        assertThat(second).isGreaterThan(first)
    }

    @Test
    fun `getReadyToSend returns entries in fifo order`() = runTest {
        dao.insert(entry("A1", entityType = OutboxEntityType.ACCOUNT))
        dao.insert(entry("T1"))
        dao.insert(entry("T2"))

        val ready = dao.getReadyToSend(now = now, staleBefore = 0, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("A1", "T1", "T2")
    }

    @Test
    fun `getReadyToSend respects the batch limit`() = runTest {
        repeat(5) { dao.insert(entry("T$it")) }

        val ready = dao.getReadyToSend(now = now, staleBefore = 0, limit = 2)

        assertThat(ready.map { it.entityLocalId }).containsExactly("T0", "T1")
    }

    /* ---------- фильтрация выдачи ---------- */

    @Test
    fun `getReadyToSend skips entries whose backoff has not elapsed`() = runTest {
        dao.insert(entry("ready", nextAttemptAt = now))
        dao.insert(entry("waiting", nextAttemptAt = now + 1))

        val ready = dao.getReadyToSend(now = now, staleBefore = 0, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("ready")
    }

    @Test
    fun `getReadyToSend skips entries that are not awaiting sending`() = runTest {
        dao.insert(entry("pending", status = OutboxStatus.PENDING))
        // Аренда ещё жива, поэтому запись в работе тоже не выбирается
        dao.insert(entry("inProgress", status = OutboxStatus.IN_PROGRESS))
        dao.insert(entry("completed", status = OutboxStatus.COMPLETED))
        dao.insert(entry("failed", status = OutboxStatus.FAILED))

        val ready = dao.getReadyToSend(now = now, staleBefore = 0, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("pending")
    }

    /* ---------- переходы статусов ---------- */

    @Test
    fun `markInProgress claims a pending entry once`() = runTest {
        val id = dao.insert(entry("T1"))

        val claimed = dao.markInProgress(sequenceNo = id, staleBefore = 0, updatedAt = now + 1)
        val claimedAgain = dao.markInProgress(sequenceNo = id, staleBefore = 0, updatedAt = now + 2)

        // Повторный захват не проходит: запись уже не PENDING — защита от двойной отправки
        assertThat(claimed).isEqualTo(1)
        assertThat(claimedAgain).isZero()
    }

    /* ---------- аренда записи, взятой в работу ---------- */

    @Test
    fun `getReadyToSend reclaims an entry whose lease expired`() = runTest {
        // Прогон взял запись и умер во время отправки — без реклейма она пропала бы навсегда
        dao.insert(entry("abandoned", status = OutboxStatus.IN_PROGRESS))

        val ready = dao.getReadyToSend(now = now, staleBefore = now, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("abandoned")
    }

    @Test
    fun `getReadyToSend leaves an entry with a live lease alone`() = runTest {
        // Её прямо сейчас отправляет другой прогон — забирать нельзя
        dao.insert(entry("inFlight", status = OutboxStatus.IN_PROGRESS))

        val ready = dao.getReadyToSend(now = now, staleBefore = now - 1, limit = 10)

        assertThat(ready).isEmpty()
    }

    @Test
    fun `markInProgress takes over an entry whose lease expired`() = runTest {
        val id = dao.insert(entry("abandoned", status = OutboxStatus.IN_PROGRESS))

        val claimed = dao.markInProgress(sequenceNo = id, staleBefore = now, updatedAt = now + 1)

        assertThat(claimed).isEqualTo(1)
    }

    @Test
    fun `markInProgress cannot steal an entry with a live lease`() = runTest {
        val id = dao.insert(entry("inFlight", status = OutboxStatus.IN_PROGRESS))

        val claimed = dao.markInProgress(sequenceNo = id, staleBefore = now - 1, updatedAt = now + 1)

        assertThat(claimed).isZero()
    }

    @Test
    fun `claiming an entry renews its lease`() = runTest {
        val id = dao.insert(entry("T1"))

        dao.markInProgress(sequenceNo = id, staleBefore = 0, updatedAt = now + 10_000)

        // Аренда продлена: по старому порогу запись уже не считается брошенной
        assertThat(dao.getReadyToSend(now = now, staleBefore = now, limit = 10)).isEmpty()
    }

    @Test
    fun `markCompleted moves entry out of the queue and clears the error`() = runTest {
        val id = dao.insert(entry("T1", status = OutboxStatus.IN_PROGRESS, lastError = "boom"))

        dao.markCompleted(sequenceNo = id, updatedAt = now + 1)

        assertThat(dao.getReadyToSend(now = now + 1, staleBefore = 0, limit = 10)).isEmpty()
        assertThat(dao.observeFailedCount().first()).isZero()
    }

    @Test
    fun `scheduleRetry returns entry to the queue after its backoff`() = runTest {
        val id = dao.insert(entry("T1", status = OutboxStatus.IN_PROGRESS))

        dao.scheduleRetry(
            sequenceNo = id,
            attemptCount = 1,
            nextAttemptAt = now + 500,
            lastError = "HTTP 503",
            updatedAt = now
        )

        assertThat(dao.getReadyToSend(now = now + 499, staleBefore = 0, limit = 10)).isEmpty()
        val ready = dao.getReadyToSend(now = now + 500, staleBefore = 0, limit = 10).single()
        assertThat(ready.attemptCount).isEqualTo(1)
        assertThat(ready.lastError).isEqualTo("HTTP 503")
    }

    @Test
    fun `markFailed moves entry to dead letter and stops retries`() = runTest {
        val id = dao.insert(entry("T1", status = OutboxStatus.IN_PROGRESS))

        dao.markFailed(sequenceNo = id, attemptCount = 3, lastError = "HTTP 400", updatedAt = now)

        assertThat(dao.getReadyToSend(now = now + 10_000, staleBefore = 0, limit = 10)).isEmpty()
        assertThat(dao.observeFailedCount().first()).isEqualTo(1)
    }

    @Test
    fun `requeueFailed returns dead letter entries to the queue with a clean slate`() = runTest {
        dao.insert(
            entry(
                "failed",
                status = OutboxStatus.FAILED,
                attemptCount = 8,
                lastError = "HTTP 400"
            )
        )

        val requeued = dao.requeueFailed(updatedAt = now + 1)

        assertThat(requeued).isEqualTo(1)
        val ready = dao.getReadyToSend(now = now + 1, staleBefore = 0, limit = 10).single()
        // Ручной повтор — утверждение «причина устранена»: прошлые неудачи не мешают отправке
        assertThat(ready.entityLocalId).isEqualTo("failed")
        assertThat(ready.attemptCount).isZero()
        assertThat(ready.nextAttemptAt).isZero()
        assertThat(ready.lastError).isNull()
    }

    @Test
    fun `requeueFailed leaves healthy entries alone`() = runTest {
        dao.insert(entry("pending", status = OutboxStatus.PENDING, nextAttemptAt = now + 5_000))
        dao.insert(entry("inProgress", status = OutboxStatus.IN_PROGRESS))

        val requeued = dao.requeueFailed(updatedAt = now + 1)

        assertThat(requeued).isZero()
        // Ожидающая backoff запись не должна внезапно стать готовой к отправке
        assertThat(dao.getReadyToSend(now = now + 1, staleBefore = 0, limit = 10)).isEmpty()
    }

    /* ---------- очистка ---------- */

    @Test
    fun `deleteCompleted removes only completed entries`() = runTest {
        dao.insert(entry("done", status = OutboxStatus.COMPLETED))
        dao.insert(entry("pending", status = OutboxStatus.PENDING))
        dao.insert(entry("failed", status = OutboxStatus.FAILED))

        dao.deleteCompleted()

        assertThat(dao.getReadyToSend(now = now, staleBefore = 0, limit = 10).map { it.entityLocalId })
            .containsExactly("pending")
        assertThat(dao.observeFailedCount().first()).isEqualTo(1)
    }

    @Test
    fun `deleteAll empties the queue`() = runTest {
        dao.insert(entry("T1"))
        dao.insert(entry("T2", status = OutboxStatus.FAILED))

        dao.deleteAll()

        assertThat(dao.getReadyToSend(now = now, staleBefore = 0, limit = 10)).isEmpty()
        assertThat(dao.observeFailedCount().first()).isZero()
    }

    /* ---------- сохранность полей ---------- */

    @Test
    fun `entry round-trips all fields through room`() = runTest {
        val id = dao.insert(
            entry(
                entityLocalId = "A1",
                entityType = OutboxEntityType.ACCOUNT,
                operation = OutboxOperation.UPDATE,
                targetServerId = "server-a1"
            )
        )

        val stored = dao.getReadyToSend(now = now, staleBefore = 0, limit = 10).single()

        assertThat(stored.sequenceNo).isEqualTo(id)
        assertThat(stored.entityType).isEqualTo(OutboxEntityType.ACCOUNT)
        assertThat(stored.operation).isEqualTo(OutboxOperation.UPDATE)
        assertThat(stored.targetServerId).isEqualTo("server-a1")
        assertThat(stored.status).isEqualTo(OutboxStatus.PENDING)
        assertThat(stored.payload).isEqualTo("""{"id":"A1"}""")
        assertThat(stored.idempotencyKey).isEqualTo("A1")
        assertThat(stored.createdAt).isEqualTo(now)
    }
}
