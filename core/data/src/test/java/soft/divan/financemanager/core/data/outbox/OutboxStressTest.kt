package soft.divan.financemanager.core.data.outbox

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import soft.divan.financemanager.core.data.testing.OpenDatabaseHolder
import soft.divan.financemanager.core.data.transaction.impl.RoomTransactionRunner
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random

/**
 * Стресс-проверка очереди на реальной цепочке под враждебной сетью.
 *
 * Отдельные тесты проверяют механику по частям на заранее заданных сценариях. Здесь очередь
 * прогоняется на случайных последовательностях операций при случайных сбоях — так ловятся
 * взаимодействия, которых не видно в отдельных сценариях: обгон операции внутри группы, потеря
 * операции при смерти процесса, повтор с другим ключом.
 *
 * Сеть подменена, всё остальное настоящее: Room, барьер порядка, аренда, backoff, dead-letter.
 * Случайность детерминирована сидом — упавший прогон воспроизводится.
 */
@RunWith(RobolectricTestRunner::class)
class OutboxStressTest {

    private companion object {
        const val ACCOUNTS = 3
        const val OPERATIONS_PER_ACCOUNT = 6
        const val MAX_ROUNDS = 60

        /** Сдвиг часов между прогонами — заведомо больше самого долгого backoff. */
        const val CLOCK_STEP_MILLIS = 2 * 60 * 60 * 1000L
    }

    /** Часы, которые тест двигает вручную: иначе backoff никогда бы не истёк. */
    private class MutableClock(var instant: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = instant
    }

    /** Запись одного ушедшего запроса — материал для проверки инвариантов. */
    private data class SentRequest(val method: String, val entityId: String, val key: String?)

    /** Сеть, которая ломается по заданному сиду. */
    private class FlakyInterceptor(private val random: Random) : Interceptor {
        val sent = mutableListOf<SentRequest>()

        /**
         * Что «есть на сервере»: клиентский id → комментарий последней принятой версии. Нужно для
         * честного read-back и для проверки, что правка доехала, а не только создание.
         */
        val serverState = mutableMapOf<String, String>()

        /** Выключает поломки — для замеров пропускной способности на исправной сети. */
        var alwaysSucceed: Boolean = false

        /** Код, которым сервер отвечает на каждую мутацию, — для заданного, а не случайного сбоя. */
        var forcedMutationCode: Int? = null

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val body = requestBody(request)
            val id = entityId(request, body)
            sent += SentRequest(request.method, id, request.header(IDEMPOTENCY_KEY_HEADER))

            val forcedCode = forcedMutationCode?.takeIf { request.method != "GET" }
            val breaksRandomly = forcedCode == null && !alwaysSucceed

            if (breaksRandomly && request.method != "GET" && random.nextInt(100) < 25) {
                throw IOException("оборвалась сеть")
            }

            val code = when {
                forcedCode != null -> forcedCode
                alwaysSucceed -> applyMutation(request.method, id, body)
                request.method == "GET" -> if (id in serverState) 200 else 404
                random.nextInt(100) < 25 -> 500
                else -> applyMutation(request.method, id, body)
            }
            return response(request, id, code)
        }

        private fun requestBody(request: Request): JsonObject? =
            request.body
                ?.let { Buffer().also { buffer -> it.writeTo(buffer) }.readUtf8() }
                ?.takeIf { it.isNotBlank() }
                ?.let { JsonParser.parseString(it).asJsonObject }

        /**
         * Идентификатор сущности запроса. У `POST` его нет в пути — он в теле, поэтому тело
         * читается. Раньше этого не делалось, и все создания выглядели одной сущностью.
         */
        private fun entityId(request: Request, body: JsonObject?): String =
            if (request.method == "POST") {
                body?.get("id")?.asString.orEmpty()
            } else {
                request.url.encodedPath.substringAfterLast('/')
            }

        /**
         * Успешная мутация меняет «состояние сервера» — чтобы read-back не врал.
         * Повтор создания сервер отвергает: дедупликацию делает первичный ключ.
         */
        private fun applyMutation(method: String, id: String, body: JsonObject?): Int {
            val comment = body?.get("comment")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            return when (method) {
                "POST" -> if (id in serverState) {
                    500
                } else {
                    serverState[id] = comment
                    200
                }

                "PUT" -> if (id in serverState) {
                    serverState[id] = comment
                    200
                } else {
                    404
                }

                else -> if (serverState.remove(id) != null) 200 else 404
            }
        }

        private fun response(request: Request, id: String, code: Int) = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("status $code")
            .body(bodyFor(request, id, code).toResponseBody("application/json".toMediaType()))
            .build()

        private fun bodyFor(request: Request, id: String, code: Int): String {
            val carriesEntity = request.method == "GET" || request.method == "POST"
            return if (code == 200 && carriesEntity) {
                transactionJson(id, serverState[id].orEmpty())
            } else {
                "{}"
            }
        }
    }

    private lateinit var db: FinanceManagerDatabase
    private lateinit var enqueuer: OutboxEnqueuer
    private lateinit var processor: OutboxProcessor
    private lateinit var interceptor: FlakyInterceptor
    private lateinit var clock: MutableClock

    private val noopContext = object : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit
        override suspend fun launchSync(block: suspend () -> Unit) = Unit
    }

    @Before
    fun setup() = setupWithSeed(seed = 20260908)

    private fun setupWithSeed(seed: Int) {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            FinanceManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        clock = MutableClock(Instant.parse("2024-05-01T10:00:00Z"))
        interceptor = FlakyInterceptor(Random(seed))

        val apiService = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(TransactionApiService::class.java)

        val holder = OpenDatabaseHolder(db)
        val outboxLocalDataSource = OutboxLocalDataSourceImpl(holder)

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
                    localDataSource = TransactionLocalDataSourceImpl(holder),
                    outboxLocalDataSource = outboxLocalDataSource,
                    gson = Gson()
                ),
                retryPolicy = OutboxRetryPolicy(),
                clock = clock,
                errorLogger = mockk<ErrorLogger>(relaxed = true),
                transactionRunner = RoomTransactionRunner(holder, noopContext)
            ),
            clock = clock
        )
    }

    @After
    fun tearDown() = db.close()

    private suspend fun insertTransaction(localId: String, accountLocalId: String) {
        db.transactionDao().insert(
            TransactionEntity(
                localId = localId,
                serverId = null,
                accountLocalId = accountLocalId,
                type = TransactionType.EXPENSE.name,
                targetAccountLocalId = null,
                accountServerId = accountLocalId,
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

    /** Последняя поставленная операция каждой сущности — по ней проверяется итог доставки. */
    private val lastOperation = mutableMapOf<String, OutboxOperation>()

    /**
     * Комментарий последней поставленной версии сущности. У правки он свой: с одинаковым телом
     * пропавшая правка выглядела бы на сервере ровно как доехавшая.
     */
    private val lastComment = mutableMapOf<String, String>()

    private suspend fun enqueue(
        localId: String,
        accountLocalId: String,
        operation: OutboxOperation
    ): Long {
        lastOperation[localId] = operation
        val comment = if (operation == OutboxOperation.UPDATE) "edited" else "lunch"
        if (operation != OutboxOperation.DELETE) lastComment[localId] = comment
        return enqueuer.enqueue(
            entityType = OutboxEntityType.TRANSACTION,
            entityLocalId = localId,
            dependencyKey = accountLocalId,
            operation = operation,
            targetServerId = if (operation == OutboxOperation.CREATE) null else localId,
            body = if (operation == OutboxOperation.DELETE) {
                null
            } else {
                TransactionRequestDto(
                    id = localId,
                    accountId = accountLocalId,
                    categoryId = "cat-1",
                    amount = BigDecimal("42.42"),
                    dateTime = "2024-01-15T10:00:00Z",
                    comment = comment
                )
            }
        )
    }

    /** Заполняет очередь: у каждого счёта своя цепочка create → update… → delete. */
    private suspend fun fillQueue(random: Random) {
        repeat(ACCOUNTS) { accountIndex ->
            val accountId = "acc-$accountIndex"
            repeat(OPERATIONS_PER_ACCOUNT) { opIndex ->
                val localId = "tx-$accountIndex-$opIndex"
                insertTransaction(localId, accountId)
                enqueue(localId, accountId, OutboxOperation.CREATE)

                if (random.nextBoolean()) enqueue(localId, accountId, OutboxOperation.UPDATE)
                if (random.nextInt(100) < 30) enqueue(localId, accountId, OutboxOperation.DELETE)
            }
        }
    }

    /** Гоняет разбор очереди, двигая часы, пока всё не устаканится. */
    private suspend fun drainUntilQuiet() {
        repeat(MAX_ROUNDS) {
            processor.process()
            clock.instant = clock.instant.plusMillis(CLOCK_STEP_MILLIS)
        }
    }

    /* ---------- инварианты ---------- */

    /** Сколько всего строк в очереди — барьер прячет часть, поэтому считаем напрямую. */
    private fun totalQueued(): Int =
        db.query("SELECT COUNT(*) FROM outbox", emptyArray()).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    @Test
    fun `an edit or delete is never sent before the creation of the same entity`() = runTest {
        fillQueue(Random(1))
        drainUntilQuiet()

        val createdAt = mutableMapOf<String, Int>()
        interceptor.sent.forEachIndexed { index, request ->
            if (request.method == "POST") createdAt.putIfAbsent(request.entityId, index)
        }

        // Каждая правка и каждое удаление обязаны идти ПОСЛЕ создания своей сущности
        interceptor.sent.forEachIndexed { index, request ->
            if (request.method == "PUT" || request.method == "DELETE") {
                assertThat(createdAt[request.entityId])
                    .`as`("%s %s ушёл раньше создания", request.method, request.entityId)
                    .isNotNull()
                    .isLessThan(index)
            }
        }
        assertThat(createdAt).isNotEmpty()
    }

    /** Сущности, у которых в очереди остались записи: ждущие, в работе или в dead-letter. */
    private fun queuedEntityIds(): Set<String> =
        db.query("SELECT DISTINCT entityLocalId FROM outbox", emptyArray()).use { cursor ->
            buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }

    @Test
    fun `no operation is silently lost`() = runTest {
        fillQueue(Random(2))
        drainUntilQuiet()

        // Сущность без записей в очереди обязана нести итог своей последней операции — и на
        // сервере, и локально. Подсчёт строк очереди этого не доказывает: пропавшая запись
        // выглядела бы в нём ровно как отправленная. Остальные сущности видны в очереди или
        // в dead-letter — молча они не пропали.
        val stillQueued = queuedEntityIds()
        val delivered = lastOperation.filterKeys { it !in stillQueued }

        assertThat(delivered).isNotEmpty()
        // Иначе проверка содержимого ниже ни разу не коснулась бы правки
        assertThat(delivered.values).contains(OutboxOperation.UPDATE)
        delivered.forEach { (localId, operation) ->
            val local = db.transactionDao().getByLocalId(localId)
            if (operation == OutboxOperation.DELETE) {
                assertThat(interceptor.serverState).`as`("%s удалена на сервере", localId)
                    .doesNotContainKey(localId)
                assertThat(local).`as`("%s удалена локально", localId).isNull()
            } else {
                // Сверяется содержимое, а не наличие: правка, пропавшая после создания, иначе
                // оставила бы тест зелёным
                assertThat(interceptor.serverState)
                    .`as`("%s на сервере в версии последней операции", localId)
                    .containsEntry(localId, lastComment.getValue(localId))
                assertThat(local?.syncStatus).`as`("%s подтверждена локально", localId)
                    .isEqualTo(SyncStatus.SYNCED)
            }
        }
    }

    @Test
    fun `every retry of an operation carries the same idempotency key`() = runTest {
        fillQueue(Random(3))
        drainUntilQuiet()

        // Один и тот же (метод, ресурс) мог уйти многократно из-за сбоев. Сервер обязан
        // распознать это как повторы одной операции — значит ключ обязан совпадать.
        val mutations = interceptor.sent.filter { it.method != "GET" }
        val keysPerOperation = mutations
            .groupBy { it.method to it.entityId }
            .mapValues { (_, requests) -> requests.mapNotNull { it.key }.distinct() }

        keysPerOperation.forEach { (operation, keys) ->
            assertThat(keys)
                .`as`("операция %s уходила с разными ключами", operation)
                .hasSizeLessThanOrEqualTo(1)
        }
        assertThat(mutations).allMatch { it.key != null }
    }

    @Test
    fun `operations of one account keep their order across the whole run`() = runTest {
        fillQueue(Random(4))
        drainUntilQuiet()

        // Все транзакции счёта лежат в одной группе, поэтому порядок обязан сохраниться
        // целиком — а не только «создание раньше правки».
        val seen = mutableMapOf<String, MutableList<String>>()
        interceptor.sent.filter { it.method != "GET" }.forEach { request ->
            // tx-<индекс счёта>-<индекс операции>
            val account = request.entityId.split('-').getOrNull(1)?.let { "acc-$it" }
                ?: return@forEach
            seen.getOrPut(account) { mutableListOf() } += "${request.method} ${request.entityId}"
        }

        assertThat(seen).isNotEmpty()
        seen.forEach { (account, log) ->
            assertThat(log.first())
                .`as`("у счёта %s первой ушла не операция создания: %s", account, log)
                .startsWith("POST")
        }
    }

    @Test
    fun `many independent operations drain in a few runs, not one per run`() = runTest {
        // Регрессия на цену барьера: он обязан держать порядок, но не сериализовать ровесников.
        // Транзакции одного счёта зависят от счёта, а не друг от друга, и должны уезжать пачкой.
        setupWithSeed(seed = 100)
        interceptor.alwaysSucceed = true

        val accountId = "acc-0"
        insertTransaction(accountId, accountId)
        enqueue(accountId, accountId, OutboxOperation.CREATE)
        repeat(30) { index ->
            val localId = "tx-0-$index"
            insertTransaction(localId, accountId)
            enqueue(localId, accountId, OutboxOperation.CREATE)
        }

        var runs = 0
        while (totalQueued() > 0 && runs < MAX_ROUNDS) {
            processor.process()
            clock.instant = clock.instant.plusMillis(CLOCK_STEP_MILLIS)
            runs++
        }

        // Одного счёта и 30 его транзакций хватает на пару проходов: счёт, затем все ровесники.
        // До исправления барьера это требовало 31 прохода и упиралось в MAX_PASSES.
        assertThat(totalQueued()).isZero()
        assertThat(runs).isLessThanOrEqualTo(2)
    }

    @Test
    fun `a run inside the backoff window asks for a retry and the retry delivers`() = runTest {
        // Вся цепочка настоящая: попытку записывает обработчик по политике повторов, а прогон
        // решает по выборке и проверке отложенных. Сбой здесь заданный, не случайный
        interceptor.forcedMutationCode = 500
        insertTransaction("tx-0-0", "acc-0")
        enqueue("tx-0-0", "acc-0", OutboxOperation.CREATE)

        assertThat(processor.process()).`as`("временный сбой").isFalse()

        // Часы не двигались: выборка пуста, но запись ждёт своего backoff. Успех здесь оборвал бы
        // цепочку повторов WorkManager, и запись ждала бы периодического синка
        interceptor.forcedMutationCode = null
        interceptor.alwaysSucceed = true
        assertThat(processor.process()).`as`("прогон в паузе backoff").isFalse()

        clock.instant = clock.instant.plusMillis(OutboxRetryPolicy.BASE_DELAY_MILLIS)
        assertThat(processor.process()).`as`("повтор после паузы").isTrue()
        assertThat(totalQueued()).isZero()
        assertThat(interceptor.serverState).containsKey("tx-0-0")
    }

    @Test
    fun `ordering holds on other random scenarios too`() = runTest {
        // Один сид мог случайно не задеть опасные комбинации — прогоняем ещё несколько
        listOf(777, 31337, 4242).forEach { seed ->
            db.close()
            setupWithSeed(seed)
            fillQueue(Random(seed))
            drainUntilQuiet()

            val createdAt = mutableMapOf<String, Int>()
            interceptor.sent.forEachIndexed { index, request ->
                if (request.method == "POST") createdAt.putIfAbsent(request.entityId, index)
            }

            interceptor.sent.forEachIndexed { index, request ->
                if (request.method == "PUT" || request.method == "DELETE") {
                    assertThat(createdAt[request.entityId])
                        .`as`(
                            "сид %s: %s %s ушёл раньше создания",
                            seed,
                            request.method,
                            request.entityId
                        )
                        .isNotNull()
                        .isLessThan(index)
                }
            }
            assertThat(createdAt).`as`("сид %s: ничего не отправилось", seed).isNotEmpty()
        }
    }
}

private fun transactionJson(id: String, comment: String) = """
    {"id":"$id","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-02-01T00:00:00Z",
     "accountId":"acc-0","categoryId":"cat-1","amount":"42.42",
     "dateTime":"2024-01-15T10:00:00Z","comment":"$comment"}
""".trimIndent()
