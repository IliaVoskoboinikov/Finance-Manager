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
        status: OutboxStatus = OutboxStatus.PENDING,
        attemptCount: Int = 0,
        nextAttemptAt: Long = 0,
        lastError: String? = null
    ) = OutboxEntryEntity(
        entityType = entityType,
        entityLocalId = entityLocalId,
        operation = operation,
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

        val ready = dao.getReadyToSend(now = now, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("A1", "T1", "T2")
    }

    @Test
    fun `getReadyToSend respects the batch limit`() = runTest {
        repeat(5) { dao.insert(entry("T$it")) }

        val ready = dao.getReadyToSend(now = now, limit = 2)

        assertThat(ready.map { it.entityLocalId }).containsExactly("T0", "T1")
    }

    /* ---------- фильтрация выдачи ---------- */

    @Test
    fun `getReadyToSend skips entries whose backoff has not elapsed`() = runTest {
        dao.insert(entry("ready", nextAttemptAt = now))
        dao.insert(entry("waiting", nextAttemptAt = now + 1))

        val ready = dao.getReadyToSend(now = now, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("ready")
    }

    @Test
    fun `getReadyToSend returns only pending entries`() = runTest {
        dao.insert(entry("pending", status = OutboxStatus.PENDING))
        dao.insert(entry("inProgress", status = OutboxStatus.IN_PROGRESS))
        dao.insert(entry("completed", status = OutboxStatus.COMPLETED))
        dao.insert(entry("failed", status = OutboxStatus.FAILED))

        val ready = dao.getReadyToSend(now = now, limit = 10)

        assertThat(ready.map { it.entityLocalId }).containsExactly("pending")
    }

    /* ---------- переходы статусов ---------- */

    @Test
    fun `markInProgress claims a pending entry once`() = runTest {
        val id = dao.insert(entry("T1"))

        val claimed = dao.markInProgress(sequenceNo = id, updatedAt = now + 1)
        val claimedAgain = dao.markInProgress(sequenceNo = id, updatedAt = now + 2)

        // Повторный захват не проходит: запись уже не PENDING — защита от двойной отправки
        assertThat(claimed).isEqualTo(1)
        assertThat(claimedAgain).isZero()
    }

    @Test
    fun `markCompleted moves entry out of the queue and clears the error`() = runTest {
        val id = dao.insert(entry("T1", status = OutboxStatus.IN_PROGRESS, lastError = "boom"))

        dao.markCompleted(sequenceNo = id, updatedAt = now + 1)

        assertThat(dao.getReadyToSend(now = now + 1, limit = 10)).isEmpty()
        assertThat(dao.observeFailed().first()).isEmpty()
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

        assertThat(dao.getReadyToSend(now = now + 499, limit = 10)).isEmpty()
        val ready = dao.getReadyToSend(now = now + 500, limit = 10).single()
        assertThat(ready.attemptCount).isEqualTo(1)
        assertThat(ready.lastError).isEqualTo("HTTP 503")
    }

    @Test
    fun `markFailed moves entry to dead letter and stops retries`() = runTest {
        val id = dao.insert(entry("T1", status = OutboxStatus.IN_PROGRESS))

        dao.markFailed(sequenceNo = id, attemptCount = 3, lastError = "HTTP 400", updatedAt = now)

        assertThat(dao.getReadyToSend(now = now + 10_000, limit = 10)).isEmpty()
        val failed = dao.observeFailed().first().single()
        assertThat(failed.entityLocalId).isEqualTo("T1")
        assertThat(failed.attemptCount).isEqualTo(3)
        assertThat(failed.lastError).isEqualTo("HTTP 400")
    }

    /* ---------- очистка ---------- */

    @Test
    fun `deleteCompleted removes only completed entries`() = runTest {
        dao.insert(entry("done", status = OutboxStatus.COMPLETED))
        dao.insert(entry("pending", status = OutboxStatus.PENDING))
        dao.insert(entry("failed", status = OutboxStatus.FAILED))

        dao.deleteCompleted()

        assertThat(dao.getReadyToSend(now = now, limit = 10).map { it.entityLocalId })
            .containsExactly("pending")
        assertThat(dao.observeFailed().first().map { it.entityLocalId }).containsExactly("failed")
    }

    @Test
    fun `deleteAll empties the queue`() = runTest {
        dao.insert(entry("T1"))
        dao.insert(entry("T2", status = OutboxStatus.FAILED))

        dao.deleteAll()

        assertThat(dao.getReadyToSend(now = now, limit = 10)).isEmpty()
        assertThat(dao.observeFailed().first()).isEmpty()
    }

    /* ---------- сохранность полей ---------- */

    @Test
    fun `entry round-trips all fields through room`() = runTest {
        val id = dao.insert(
            entry(
                entityLocalId = "A1",
                entityType = OutboxEntityType.ACCOUNT,
                operation = OutboxOperation.UPDATE
            )
        )

        val stored = dao.getReadyToSend(now = now, limit = 10).single()

        assertThat(stored.sequenceNo).isEqualTo(id)
        assertThat(stored.entityType).isEqualTo(OutboxEntityType.ACCOUNT)
        assertThat(stored.operation).isEqualTo(OutboxOperation.UPDATE)
        assertThat(stored.status).isEqualTo(OutboxStatus.PENDING)
        assertThat(stored.payload).isEqualTo("""{"id":"A1"}""")
        assertThat(stored.idempotencyKey).isEqualTo("A1")
        assertThat(stored.createdAt).isEqualTo(now)
    }
}
