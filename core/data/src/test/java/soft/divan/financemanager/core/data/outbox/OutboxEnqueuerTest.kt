package soft.divan.financemanager.core.data.outbox

import com.google.gson.Gson
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Тесты [OutboxEnqueuer]: из чего складывается запись очереди — снимок тела, ключ идемпотентности
 * и стартовые метаданные ретраев.
 */
class OutboxEnqueuerTest {

    private companion object {
        val UUID_PATTERN =
            Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").toPattern()
    }

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val localDataSource = mockk<OutboxLocalDataSource>()

    private val processor = mockk<OutboxProcessor>(relaxed = true)

    /** Выполняет отложенные действия сразу — здесь проверяется не порядок, а содержимое записи. */
    private val appCoroutineContext = object : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit
        override suspend fun launchSync(block: suspend () -> Unit) = block()
    }

    private val enqueuer = OutboxEnqueuer(
        localDataSource = localDataSource,
        gson = Gson(),
        clock = clock,
        appCoroutineContext = appCoroutineContext,
        processor = { processor }
    )

    private fun captureEnqueued(): CapturingSlot<OutboxEntryEntity> {
        val slot = slot<OutboxEntryEntity>()
        coEvery { localDataSource.enqueue(capture(slot)) } returns 1L
        return slot
    }

    /** Копит все поставленные записи — для проверок, где важно отличие операций друг от друга. */
    private fun captureAllEnqueued(): MutableList<OutboxEntryEntity> {
        val captured = mutableListOf<OutboxEntryEntity>()
        coEvery { localDataSource.enqueue(capture(captured)) } returns 1L
        return captured
    }

    private fun requestDto() = TransactionRequestDto(
        id = "local-t1",
        accountId = "local-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    @Test
    fun `enqueue stores the dependency group of the operation`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        // Транзакция входит в группу своего счёта, а не в собственную
        assertThat(enqueued.captured.dependencyKey).isEqualTo("local-a1")
        assertThat(enqueued.captured.entityLocalId).isEqualTo("local-t1")
    }

    @Test
    fun `enqueue stores a json snapshot of the request body`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        val payload = Gson().fromJson(enqueued.captured.payload, Map::class.java)
        assertThat(payload["id"]).isEqualTo("local-t1")
        assertThat(payload["accountId"]).isEqualTo("local-a1")
        assertThat(payload["comment"]).isEqualTo("lunch")
    }

    @Test
    fun `idempotency key is separate from the entity id`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        // entityLocalId адресует сущность, ключ — намерение её изменить: это разные вещи
        assertThat(enqueued.captured.entityLocalId).isEqualTo("local-t1")
        assertThat(enqueued.captured.idempotencyKey).isNotEqualTo("local-t1")
        assertThat(enqueued.captured.idempotencyKey).matches(UUID_PATTERN)
    }

    @Test
    fun `operations on the same entity get different idempotency keys`() = runTest {
        val enqueued = captureAllEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )
        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.UPDATE,
            targetServerId = "server-t1",
            body = requestDto()
        )

        // Совпади ключи — сервер счёл бы правку повтором создания и молча её проглотил
        assertThat(enqueued).hasSize(2)
        assertThat(enqueued[0].idempotencyKey).isNotEqualTo(enqueued[1].idempotencyKey)
        assertThat(enqueued.map { it.entityLocalId }).containsExactly("local-t1", "local-t1")
    }

    @Test
    fun `repeated identical operations still get their own keys`() = runTest {
        val enqueued = captureAllEnqueued()

        repeat(3) {
            enqueuer.enqueue(
                entityType = OutboxEntityType.TRANSACTION,
                entityLocalId = "local-t1",
                dependencyKey = "local-a1",
                operation = OutboxOperation.UPDATE,
                targetServerId = "server-t1",
                body = requestDto()
            )
        }

        // Три сознательные правки — три разные операции, а не один повтор
        assertThat(enqueued.map { it.idempotencyKey }.distinct()).hasSize(3)
    }

    @Test
    fun `enqueue starts an entry as pending and immediately sendable`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.ACCOUNT,
            entityLocalId = "local-a1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        assertThat(enqueued.captured.status).isEqualTo(OutboxStatus.PENDING)
        assertThat(enqueued.captured.attemptCount).isZero()
        assertThat(enqueued.captured.nextAttemptAt).isZero()
        assertThat(enqueued.captured.lastError).isNull()
        assertThat(enqueued.captured.createdAt).isEqualTo(now.toEpochMilli())
        assertThat(enqueued.captured.updatedAt).isEqualTo(now.toEpochMilli())
    }

    @Test
    fun `enqueue keeps the target server id for addressable operations`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.UPDATE,
            targetServerId = "server-t1",
            body = requestDto()
        )

        assertThat(enqueued.captured.targetServerId).isEqualTo("server-t1")
        assertThat(enqueued.captured.operation).isEqualTo(OutboxOperation.UPDATE)
    }

    @Test
    fun `enqueue writes an empty payload for bodiless operations`() = runTest {
        val enqueued = captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.DELETE,
            targetServerId = "server-t1"
        )

        assertThat(enqueued.captured.payload).isEqualTo("{}")
        assertThat(enqueued.captured.targetServerId).isEqualTo("server-t1")
    }

    @Test
    fun `enqueue schedules processing so the operation is not left waiting`() = runTest {
        captureEnqueued()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        coVerify(exactly = 1) { processor.process() }
    }

    @Test
    fun `enqueue returns the assigned sequence number`() = runTest {
        coEvery { localDataSource.enqueue(any()) } returns 42L

        val sequenceNo = enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = "local-t1",
            dependencyKey = "local-a1",
            operation = OutboxOperation.CREATE,
            body = requestDto()
        )

        assertThat(sequenceNo).isEqualTo(42L)
    }
}
