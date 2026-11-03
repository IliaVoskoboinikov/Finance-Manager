package soft.divan.financemanager.core.data.outbox

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
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
import soft.divan.financemanager.core.auth.data.interceptor.GuestModeNetworkBlockedException
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.outbox.impl.AccountOutboxSender
import soft.divan.financemanager.core.data.outbox.impl.RoutingOutboxSender
import soft.divan.financemanager.core.data.outbox.impl.TransactionOutboxSender
import soft.divan.financemanager.core.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.core.data.repository.TransactionRepositoryImpl
import soft.divan.financemanager.core.data.source.impl.AccountLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.AccountRemoteDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.core.data.testing.OpenDatabaseHolder
import soft.divan.financemanager.core.data.transaction.impl.RoomTransactionRunner
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Сквозная проверка операций счёта, у которого ещё нет `serverId`: гость или офлайн.
 *
 * Сценарий из аудита (C4): гость ведёт учёт, потом входит с объединением данных. До outbox
 * (FM-218) транзакция хранила копию `serverId` счёта, снятую при создании, — `null`, — и её
 * отправка молча пропускалась навсегда; тест сторожит, чтобы это не вернулось. Здесь собрана
 * настоящая цепочка — репозитории, Room, очередь,
 * процессор, отправители, Retrofit — и подменена только сеть: проверяется, что после входа
 * уезжают и счёт, и его операция, причём операция — с тем id счёта, под которым его создал
 * сервер.
 */
@RunWith(RobolectricTestRunner::class)
class UnsyncedAccountOutboxEndToEndTest {

    /** Сервер-заглушка: записывает мутации и отвечает созданной записью с клиентским id. */
    private class FakeServer : Interceptor {
        /** Гостевой режим: сеть закрыта до сервера, как у `GuestAccessInterceptor`. */
        var isGuest = true
        val requests = mutableListOf<Pair<String, JsonObject>>()

        override fun intercept(chain: Interceptor.Chain): Response {
            if (isGuest) throw GuestModeNetworkBlockedException()

            val request = chain.request()
            val body = Buffer().also { request.body?.writeTo(it) }.readUtf8()
                .let { JsonParser.parseString(it).asJsonObject }
            val path = request.url.encodedPath
            requests += "${request.method} $path" to body

            val response = if (path.endsWith("/account")) accountJson(body) else transactionJson(body)
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(response.toResponseBody("application/json".toMediaType()))
                .build()
        }

        private fun accountJson(body: JsonObject) = """
            {"id":${body["id"]},"userId":"user-1","name":${body["name"]},
             "balance":${body["balance"]},"currencyId":${body["currencyId"]},
             "createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}
        """.trimIndent()

        private fun transactionJson(body: JsonObject) = """
            {"id":${body["id"]},"createdAt":"2024-01-01T00:00:00Z",
             "updatedAt":"2024-01-01T00:00:00Z","accountId":${body["accountId"]},
             "categoryId":${body["categoryId"]},"amount":${body["amount"]},
             "dateTime":${body["dateTime"]},"comment":${body["comment"]}}
        """.trimIndent()
    }

    private companion object {
        const val ACCOUNT_ID = "local-a1"
        const val TRANSACTION_ID = "local-t1"
        val CREATED_AT: Instant = Instant.parse("2024-01-01T00:00:00Z")
    }

    private val clock = Clock.fixed(Instant.parse("2024-05-01T10:00:00Z"), ZoneOffset.UTC)
    private val server = FakeServer()

    /** Разбор очереди запускаем в тесте вручную, чтобы наблюдать каждый прогон отдельно. */
    private val noopContext = object : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit
        override suspend fun launchSync(block: suspend () -> Unit) = Unit
    }

    private lateinit var db: FinanceManagerDatabase
    private lateinit var processor: OutboxProcessor
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl

    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            FinanceManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        val holder = OpenDatabaseHolder(db)
        val outboxLocalDataSource = OutboxLocalDataSourceImpl(holder)
        val accountLocalDataSource = AccountLocalDataSourceImpl(holder)
        val transactionLocalDataSource = TransactionLocalDataSourceImpl(holder)
        val transactionRunner = RoomTransactionRunner(holder, noopContext)

        val enqueuer = OutboxEnqueuer(
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
                sender = routingSender(
                    accountLocalDataSource,
                    transactionLocalDataSource,
                    outboxLocalDataSource
                ),
                retryPolicy = OutboxRetryPolicy(),
                clock = clock,
                errorLogger = errorLogger,
                transactionRunner = transactionRunner
            ),
            clock = clock
        )

        accountRepository = AccountRepositoryImpl(
            remoteDataSource = mockk(relaxed = true),
            localDataSource = accountLocalDataSource,
            transactionLocalDataSource = transactionLocalDataSource,
            syncManager = mockk(relaxed = true),
            transactionRunner = transactionRunner,
            outboxEnqueuer = enqueuer,
            outboxLocalDataSource = outboxLocalDataSource,
            appCoroutineContext = noopContext,
            errorLogger = errorLogger
        )

        transactionRepository = TransactionRepositoryImpl(
            remoteDataSource = mockk(relaxed = true),
            localDataSource = transactionLocalDataSource,
            accountLocalDataSource = accountLocalDataSource,
            categoryLocalDataSource = mockk(relaxed = true),
            syncManager = mockk(relaxed = true),
            transactionRunner = transactionRunner,
            outboxEnqueuer = enqueuer,
            outboxLocalDataSource = outboxLocalDataSource,
            appCoroutineContext = noopContext,
            errorLogger = errorLogger
        )
    }

    @After
    fun tearDown() = db.close()

    /** Настоящие отправители счёта и операции поверх Retrofit, у которого подменена только сеть. */
    private fun routingSender(
        accountLocalDataSource: AccountLocalDataSourceImpl,
        transactionLocalDataSource: TransactionLocalDataSourceImpl,
        outboxLocalDataSource: OutboxLocalDataSourceImpl
    ): RoutingOutboxSender {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .client(OkHttpClient.Builder().addInterceptor(server).build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        return RoutingOutboxSender(
            transactionSender = TransactionOutboxSender(
                remoteDataSource = TransactionRemoteDataSourceImpl(
                    retrofit.create(TransactionApiService::class.java)
                ),
                localDataSource = transactionLocalDataSource,
                outboxLocalDataSource = outboxLocalDataSource,
                gson = Gson()
            ),
            accountSender = AccountOutboxSender(
                remoteDataSource = AccountRemoteDataSourceImpl(
                    retrofit.create(AccountApiService::class.java)
                ),
                localDataSource = accountLocalDataSource,
                outboxLocalDataSource = outboxLocalDataSource,
                gson = Gson()
            )
        )
    }

    private val guestTransaction = Transaction(
        id = TRANSACTION_ID,
        accountLocalId = ACCOUNT_ID,
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse("2024-01-15T10:00:00Z"),
        comment = "обед",
        createdAt = CREATED_AT,
        updatedAt = CREATED_AT
    )

    /** Гость заводит счёт и операцию на нём — у счёта нет `serverId`. */
    private suspend fun recordAsGuest() {
        val account = Account(
            id = ACCOUNT_ID,
            name = "Карта",
            balance = BigDecimal("100.00"),
            currencyId = "rub-id",
            createdAt = CREATED_AT,
            updatedAt = CREATED_AT
        )
        assertThat(accountRepository.create(account)).isEqualTo(DomainResult.Success(Unit))
        assertThat(transactionRepository.create(guestTransaction))
            .isEqualTo(DomainResult.Success(Unit))
    }

    @Test
    fun `operations of a guest account wait in the queue while the network is blocked`() = runTest {
        recordAsGuest()

        processor.process()

        assertThat(server.requests).isEmpty()
        assertThat(db.outboxDao().countUnsent()).isEqualTo(2)
        assertThat(db.transactionDao().getByLocalId(TRANSACTION_ID)?.syncStatus)
            .isEqualTo(SyncStatus.PENDING_CREATE)
    }

    @Test
    fun `after sign-in the account goes first and its operation follows under the same id`() =
        runTest {
            recordAsGuest()
            processor.process()

            // Вход с объединением данных: сеть открыта, очередь доотправляет гостевые операции
            server.isGuest = false
            processor.process()

            assertThat(server.requests.map { it.first }).containsExactly(
                "POST /api/v1/account",
                "POST /api/v1/transaction"
            )
            val accountServerId = server.requests[0].second["id"].asString
            assertThat(accountServerId).isEqualTo(ACCOUNT_ID)
            // Операция адресует счёт тем id, под которым его только что создал сервер
            assertThat(server.requests[1].second["accountId"].asString).isEqualTo(accountServerId)

            assertThat(db.outboxDao().countUnsent()).isZero()
            // countUnsent не видит dead-letter: «ушло» и «осело в FAILED» проверяются отдельно
            assertThat(db.outboxDao().observeFailedCount().first()).isZero()
            val account = db.accountDao().getByLocalId(ACCOUNT_ID)
            assertThat(account?.serverId).isEqualTo(accountServerId)
            assertThat(account?.syncStatus).isEqualTo(SyncStatus.SYNCED)
            val transaction = db.transactionDao().getByLocalId(TRANSACTION_ID)
            assertThat(transaction?.serverId).isEqualTo(TRANSACTION_ID)
            assertThat(transaction?.syncStatus).isEqualTo(SyncStatus.SYNCED)
        }

    @Test
    fun `an operation on an account pulled from the server goes with the account's serverId`() =
        runTest {
            // Счёт пришёл pull'ом с другого устройства: его serverId не совпадает с localId
            db.accountDao().insert(
                AccountEntity(
                    localId = ACCOUNT_ID,
                    serverId = "srv-a1",
                    name = "Карта",
                    balance = "100.00",
                    currencyId = "rub-id",
                    createdAt = "2024-01-01T00:00:00Z",
                    updatedAt = "2024-01-01T00:00:00Z",
                    syncStatus = SyncStatus.SYNCED
                )
            )
            server.isGuest = false

            assertThat(transactionRepository.create(guestTransaction))
                .isEqualTo(DomainResult.Success(Unit))
            processor.process()

            assertThat(server.requests.map { it.first }).containsExactly("POST /api/v1/transaction")
            assertThat(server.requests[0].second["accountId"].asString).isEqualTo("srv-a1")
        }

    @Test
    fun `a guest edit of the operation follows its creation with the same account id`() =
        runTest {
            recordAsGuest()
            val edited = guestTransaction.copy(comment = "ужин")
            assertThat(transactionRepository.update(edited)).isEqualTo(DomainResult.Success(Unit))
            processor.process()

            server.isGuest = false
            processor.process()

            assertThat(server.requests.map { it.first }).containsExactly(
                "POST /api/v1/account",
                "POST /api/v1/transaction",
                // До подтверждения создания операция адресуется своим клиентским id
                "PUT /api/v1/transaction/$TRANSACTION_ID"
            )
            val edit = server.requests[2].second
            assertThat(edit["accountId"].asString).isEqualTo(ACCOUNT_ID)
            assertThat(edit["comment"].asString).isEqualTo("ужин")
            assertThat(db.outboxDao().countUnsent()).isZero()
            assertThat(db.outboxDao().observeFailedCount().first()).isZero()
            assertThat(db.transactionDao().getByLocalId(TRANSACTION_ID)?.syncStatus)
                .isEqualTo(SyncStatus.SYNCED)
        }
}
