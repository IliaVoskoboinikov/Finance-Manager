package soft.divan.financemanager.core.data.outbox

import androidx.room.Room
import com.google.gson.Gson
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.core.data.api.IDEMPOTENCY_KEY_HEADER
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.outbox.impl.TransactionOutboxSender
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Контракт клиента перед серверной сверкой payload (замечание ревьюера №2).
 *
 * Чтобы сервер мог сверять тело запроса с сохранённым по ключу, клиент обязан гарантировать:
 * один ключ — всегда одно и то же тело. Здесь эта гарантия проверяется на настоящей цепочке
 * (Room → очередь → отправитель → Retrofit), потому что нарушить её может любое звено: достаточно
 * пересобрать тело из текущей строки вместо снимка, и повтор поедет с другим содержимым.
 *
 * Обратная сторона контракта — чем клиент доказывает себе, что операция долетела: read-back
 * опирается на **клиентский `id`**, уникальный по построению, поэтому наличие записи на сервере
 * и есть доказательство. Сверка содержимого на это ответа не даёт: расхождение означает не
 * «мой POST не долетел», а «после него запись изменили», и это уже вопрос last-write-wins.
 */
@RunWith(RobolectricTestRunner::class)
class OutboxPayloadContractTest {

    /** Записывает тела и заголовки ушедших запросов; сеть не нужна. */
    private class RecordingInterceptor : Interceptor {
        val requests = mutableListOf<Request>()
        val bodies = mutableListOf<String>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requests += request
            bodies += request.body?.let { body ->
                Buffer().also { body.writeTo(it) }.readUtf8()
            }.orEmpty()

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Server Error")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private companion object {
        const val LOCAL_ID = "local-t1"
    }

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private lateinit var db: FinanceManagerDatabase
    private lateinit var enqueuer: OutboxEnqueuer
    private lateinit var sender: TransactionOutboxSender
    private val interceptor = RecordingInterceptor()

    private val noopContext = object : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit
        override suspend fun launchSync(block: suspend () -> Unit) = Unit
    }

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            FinanceManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        val apiService = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(TransactionApiService::class.java)

        enqueuer = OutboxEnqueuer(
            localDataSource = OutboxLocalDataSourceImpl(db.outboxDao()),
            gson = Gson(),
            clock = clock,
            appCoroutineContext = noopContext,
            processor = { mockk<OutboxProcessor>(relaxed = true) }
        )

        sender = TransactionOutboxSender(
            remoteDataSource = TransactionRemoteDataSourceImpl(apiService),
            localDataSource = TransactionLocalDataSourceImpl(db.transactionDao()),
            gson = Gson()
        )

        mockk<ErrorLogger>(relaxed = true)
    }

    @After
    fun tearDown() = db.close()

    private fun entity(amount: String, comment: String) = TransactionEntity(
        localId = LOCAL_ID,
        serverId = null,
        accountLocalId = "local-a1",
        type = TransactionType.EXPENSE.name,
        targetAccountLocalId = null,
        accountServerId = "local-a1",
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = amount,
        transactionDate = "2024-01-15T10:00:00Z",
        comment = comment,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.PENDING_CREATE
    )

    private fun requestDto(amount: String, comment: String) = TransactionRequestDto(
        id = LOCAL_ID,
        accountId = "local-a1",
        categoryId = "cat-1",
        amount = BigDecimal(amount),
        dateTime = "2024-01-15T10:00:00Z",
        comment = comment
    )

    private suspend fun enqueueCreate(
        amount: String,
        comment: String,
        entityLocalId: String = LOCAL_ID
    ): Long = enqueuer.enqueue(
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = entityLocalId,
        dependencyKey = "local-a1",
        operation = OutboxOperation.CREATE,
        body = requestDto(amount, comment)
    )

    private suspend fun storedEntry(sequenceNo: Long) =
        db.outboxDao().getReadyToSend(now = now.toEpochMilli(), staleBefore = 0, limit = 50)
            .first { it.sequenceNo == sequenceNo }

    private val sentKeys get() = interceptor.requests
        .filter { it.method == "POST" }
        .map { it.header(IDEMPOTENCY_KEY_HEADER) }

    /** Только тела мутирующих запросов: следом за неудачным POST идёт ещё read-back без тела. */
    private val sentBodies get() = interceptor.requests
        .withIndex()
        .filter { (_, request) -> request.method == "POST" }
        .map { (index, _) -> interceptor.bodies[index] }

    @Test
    fun `same key always carries a byte-identical body`() = runTest {
        db.transactionDao().insert(entity("42.42", "lunch"))
        val entry = storedEntry(enqueueCreate("42.42", "lunch"))

        // Три попытки отправки одной и той же записи очереди
        repeat(3) { sender.send(entry) }

        assertThat(sentBodies).hasSize(3)
        assertThat(sentBodies.distinct()).hasSize(1)
        assertThat(sentKeys.distinct()).hasSize(1)
    }

    @Test
    fun `body is the snapshot, not the current row`() = runTest {
        db.transactionDao().insert(entity("42.42", "lunch"))
        val entry = storedEntry(enqueueCreate("42.42", "lunch"))

        sender.send(entry)
        // Пользователь правит строку между попытками — снимок операции меняться не должен
        db.transactionDao().update(entity("999.99", "изменено"))
        sender.send(entry)

        assertThat(sentBodies.distinct()).hasSize(1)
        assertThat(sentBodies.first()).contains("42.42")
        assertThat(sentBodies.first()).doesNotContain("999.99")
    }

    @Test
    fun `a different body always travels under a different key`() = runTest {
        db.transactionDao().insert(entity("42.42", "lunch"))
        // Разные строки: иначе барьер не выпустит вторую операцию, пока не закрыта первая
        val first = storedEntry(enqueueCreate("42.42", "lunch"))
        val second = storedEntry(enqueueCreate("100.00", "ужин", entityLocalId = "local-t2"))

        sender.send(first)
        sender.send(second)

        // Ключ и тело — неразрывная пара: сверка на сервере не может ложно сработать
        assertThat(sentBodies[0]).isNotEqualTo(sentBodies[1])
        assertThat(sentKeys[0]).isNotEqualTo(sentKeys[1])
    }

    @Test
    fun `key and payload are frozen together at enqueue time`() = runTest {
        db.transactionDao().insert(entity("42.42", "lunch"))
        val sequenceNo = enqueueCreate("42.42", "lunch")

        val before = storedEntry(sequenceNo)
        db.transactionDao().update(entity("999.99", "изменено"))
        val after = storedEntry(sequenceNo)

        // Правка доменной строки не трогает ни ключ, ни снимок тела
        assertThat(after.idempotencyKey).isEqualTo(before.idempotencyKey)
        assertThat(after.payload).isEqualTo(before.payload)
        assertThat(after.payload).contains("42.42")
    }

    @Test
    fun `read-back proves delivery by the client generated id`() = runTest {
        db.transactionDao().insert(entity("42.42", "lunch"))
        val entry = storedEntry(enqueueCreate("42.42", "lunch"))

        sender.send(entry)

        // POST упал 500 → перепроверка идёт по клиентскому id: он уникален по построению,
        // поэтому запись под ним на сервере может быть только нашей
        val readBack = interceptor.requests.last()
        assertThat(readBack.method).isEqualTo("GET")
        assertThat(readBack.url.encodedPath).endsWith("/$LOCAL_ID")
        assertThat(entry.entityLocalId).isEqualTo(LOCAL_ID)
    }
}
