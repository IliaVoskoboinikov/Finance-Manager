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
import soft.divan.financemanager.core.data.transaction.impl.RoomTransactionRunner
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
 * Сквозная проверка ключа идемпотентности: от постановки операции в очередь до HTTP-заголовка.
 *
 * Собирается настоящая цепочка — Room, очередь, процессор, отправитель, Retrofit — и подменяется
 * только сеть. Отдельные звенья покрыты своими тестами; здесь проверяется их **композиция**:
 * что ключ, сгенерированный при постановке в очередь, доезжает до провода неизменным, а разные
 * операции над одной сущностью уходят с разными ключами.
 */
@RunWith(RobolectricTestRunner::class)
class OutboxIdempotencyKeyEndToEndTest {

    /** Записывает все ушедшие запросы и отвечает вместо сети. */
    private class RecordingInterceptor : Interceptor {
        val requests = mutableListOf<Request>()

        /** Коды ответа: отдельно для мутаций и для чтения (read-back). */
        var mutationCode: Int = 200
        var readCode: Int = 200

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requests += request

            val isRead = request.method == "GET"
            val code = if (isRead) readCode else mutationCode
            val body = if (request.method == "POST" || isRead) TRANSACTION_JSON else "{}"

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("status $code")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private companion object {
        const val LOCAL_ID = "local-t1"
        val TRANSACTION_JSON = """
            {"id":"$LOCAL_ID","createdAt":"2024-01-01T00:00:00Z",
             "updatedAt":"2024-02-01T00:00:00Z","accountId":"local-a1","categoryId":"cat-1",
             "amount":"42.42","dateTime":"2024-01-15T10:00:00Z","comment":"lunch"}
        """.trimIndent()
    }

    private val now = Instant.parse("2024-05-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private lateinit var db: FinanceManagerDatabase
    private lateinit var enqueuer: OutboxEnqueuer
    private lateinit var processor: OutboxProcessor
    private val interceptor = RecordingInterceptor()

    /** Разбор очереди запускаем в тесте вручную, чтобы наблюдать каждый прогон отдельно. */
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

        val outboxLocalDataSource = OutboxLocalDataSourceImpl(db.outboxDao())

        enqueuer = OutboxEnqueuer(
            localDataSource = outboxLocalDataSource,
            gson = Gson(),
            clock = clock,
            appCoroutineContext = noopContext,
            processor = { processor }
        )

        processor = OutboxProcessor(
            localDataSource = outboxLocalDataSource,
            entryHandler = OutboxEntryHandler(
                localDataSource = outboxLocalDataSource,
                sender = TransactionOutboxSender(
                    remoteDataSource = TransactionRemoteDataSourceImpl(apiService),
                    localDataSource = TransactionLocalDataSourceImpl(db.transactionDao()),
                    gson = Gson()
                ),
                retryPolicy = OutboxRetryPolicy(),
                clock = clock,
                errorLogger = mockk<ErrorLogger>(relaxed = true),
                // Настоящий runner: обратный путь закрывается одной транзакцией на живом Room
                transactionRunner = RoomTransactionRunner(db, noopContext)
            ),
            clock = clock
        )
    }

    @After
    fun tearDown() = db.close()

    private suspend fun insertLocalTransaction() {
        db.transactionDao().insert(
            TransactionEntity(
                localId = LOCAL_ID,
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
        )
    }

    private fun requestDto() = TransactionRequestDto(
        id = LOCAL_ID,
        accountId = "local-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    private suspend fun enqueueCreate() = enqueuer.enqueue(
        entityType = OutboxEntityType.TRANSACTION,
        entityLocalId = LOCAL_ID,
        dependencyKey = "local-a1",
        operation = OutboxOperation.CREATE,
        body = requestDto()
    )

    private suspend fun storedKey(sequenceNo: Long): String =
        db.outboxDao().getReadyToSend(now = now.toEpochMilli(), staleBefore = 0, limit = 50)
            .first { it.sequenceNo == sequenceNo }
            .idempotencyKey

    private val sentKeys get() = interceptor.requests.map { it.header(IDEMPOTENCY_KEY_HEADER) }

    @Test
    fun `key generated at enqueue reaches the wire unchanged`() = runTest {
        insertLocalTransaction()
        val sequenceNo = enqueueCreate()
        val expectedKey = storedKey(sequenceNo)

        processor.process()

        assertThat(sentKeys).containsExactly(expectedKey)
        // Ключ операции — не id сущности: это разные адреса
        assertThat(expectedKey).isNotEqualTo(LOCAL_ID)
    }

    @Test
    fun `create and update of the same entity go out with different keys`() = runTest {
        insertLocalTransaction()
        enqueueCreate()
        processor.process()

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = LOCAL_ID,
            dependencyKey = "local-a1",
            operation = OutboxOperation.UPDATE,
            targetServerId = LOCAL_ID,
            body = requestDto()
        )
        processor.process()

        // Совпади ключи — сервер счёл бы правку повтором создания и потерял бы её
        assertThat(sentKeys).hasSize(2)
        assertThat(sentKeys[0]).isNotEqualTo(sentKeys[1])
        assertThat(sentKeys).doesNotContainNull()
    }

    /* ---------- регрессия: порядок внутри группы ---------- */

    @Test
    fun `an edit never overtakes the creation that is waiting in backoff`() = runTest {
        insertLocalTransaction()
        // Создание не удалось и ушло в backoff
        interceptor.mutationCode = 500
        interceptor.readCode = 404
        enqueueCreate()
        processor.process()

        // Пользователь правит ту же транзакцию — правка готова к отправке немедленно
        interceptor.mutationCode = 200
        interceptor.readCode = 200
        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = LOCAL_ID,
            dependencyKey = "local-a1",
            operation = OutboxOperation.UPDATE,
            targetServerId = LOCAL_ID,
            body = requestDto()
        )
        processor.process()

        // РЕГРЕССИЯ: раньше правка обгоняла создание, уходила PUT по несуществующему id,
        // получала 404 и уезжала в dead-letter — правка пользователя терялась
        assertThat(interceptor.requests.map { it.method }).doesNotContain("PUT")
    }

    @Test
    fun `a delete never overtakes the creation holding a live lease`() = runTest {
        insertLocalTransaction()
        val sequenceNo = enqueueCreate()
        // Создание взято в работу и удерживает аренду: прогон его ещё не завершил
        db.outboxDao().markInProgress(sequenceNo, staleBefore = 0, updatedAt = now.toEpochMilli())

        enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = LOCAL_ID,
            dependencyKey = "local-a1",
            operation = OutboxOperation.DELETE,
            targetServerId = LOCAL_ID
        )
        processor.process()

        // РЕГРЕССИЯ: раньше удаление уходило раньше создания, получало 404 = «успех»,
        // строка удалялась локально, а создание позже оставляло на сервере фантом
        assertThat(interceptor.requests.map { it.method }).doesNotContain("DELETE")
    }

    @Test
    fun `resend after a lost lease reuses the same key`() = runTest {
        insertLocalTransaction()
        val sequenceNo = enqueueCreate()
        val expectedKey = storedKey(sequenceNo)

        // Прогон взял запись и умер, не доложив об исходе; аренда истекла — запись уходит снова
        db.outboxDao().markInProgress(sequenceNo, staleBefore = 0, updatedAt = 0)
        processor.process()

        // Повтор обязан нести ТОТ ЖЕ ключ — иначе сервер не распознает повторную доставку
        assertThat(sentKeys).containsExactly(expectedKey)
    }
}
