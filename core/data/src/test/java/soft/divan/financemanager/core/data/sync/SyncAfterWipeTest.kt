package soft.divan.financemanager.core.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.outbox.OutboxEntryHandler
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.outbox.OutboxRetryPolicy
import soft.divan.financemanager.core.data.outbox.OutboxSendResult
import soft.divan.financemanager.core.data.outbox.OutboxSender
import soft.divan.financemanager.core.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.core.data.repository.RecordingAppCoroutineContext
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.AccountLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.CategoryLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.core.data.sync.impl.AccountSyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.CategorySyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.TransactionSyncManagerImpl
import soft.divan.financemanager.core.data.testing.VaultHarness
import soft.divan.financemanager.core.data.transaction.impl.RoomTransactionRunner
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.holder.StaleDataException
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.io.File
import java.math.BigDecimal
import java.time.Clock

/**
 * Выход из аккаунта посреди синка: сервер отвечает уже после крипто-стирания.
 *
 * Запрос уходит без аренды базы, поэтому стирание его не ждёт; ответ, записанный после него,
 * попал бы в новую базу — к гостю или следующему пользователю. Здесь настоящие хранилище ключей,
 * холдер и источники данных, а сеть — моки, которые отвечают, когда скажет тест.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SyncAfterWipeTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dispatcher = StandardTestDispatcher()
    private val harness by lazy {
        VaultHarness(context, File(folder.root, "keyset.preferences_pb"), dispatcher)
    }
    private val holder get() = harness.holder
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val accounts by lazy { AccountLocalDataSourceImpl(holder) }
    private val categories by lazy { CategoryLocalDataSourceImpl(holder) }
    private val transactions by lazy { TransactionLocalDataSourceImpl(holder) }
    private val outbox by lazy { OutboxLocalDataSourceImpl(holder) }

    /** Запрос дошёл до сервера — можно стирать. */
    private val requested = CompletableDeferred<Unit>()

    @Test
    fun `accounts answered after the wipe stay out of the new database`() = wipeTest {
        val response = CompletableDeferred<Response<List<AccountDto>>>()
        val remote = mockk<AccountRemoteDataSource> {
            coEvery { getAll() } coAnswers { awaitAnswer(response) }
        }
        val manager = AccountSyncManagerImpl(remote, accounts, holder, errorLogger)

        val pulled = async { manager.pullServerData() }
        requested.await()
        harness.vault.wipe()
        response.complete(Response.success(listOf(accountDto())))

        assertThat(pulled.await()).isFalse()
        assertThat(holder.withDatabase { it.accountDao().getAll().first() }).isEmpty()
    }

    @Test
    fun `categories answered after the wipe stay out of the new database`() = wipeTest {
        val response = CompletableDeferred<Response<List<CategoryDto>>>()
        val remote = mockk<CategoryRemoteDataSource> {
            coEvery { getAll() } coAnswers { awaitAnswer(response) }
        }
        val manager = CategorySyncManagerImpl(remote, categories, holder, errorLogger)

        val pulled = async { manager.pullServerData() }
        requested.await()
        harness.vault.wipe()
        response.complete(Response.success(listOf(categoryDto())))

        assertThat(pulled.await()).isFalse()
        assertThat(holder.withDatabase { it.categoryDao().getById(FOREIGN_CATEGORY) }).isNull()
    }

    @Test
    fun `categories by type answered after the wipe stay out of the new database`() = wipeTest {
        val response = CompletableDeferred<Response<List<CategoryDto>>>()
        val remote = mockk<CategoryRemoteDataSource> {
            coEvery { getByType(any()) } coAnswers { awaitAnswer(response) }
        }
        val background = RecordingAppCoroutineContext()
        val repository = CategoryRepositoryImpl(
            remoteDataSource = remote,
            localDataSource = categories,
            syncManager = mockk(relaxed = true),
            appCoroutineContext = background,
            dataGuard = holder,
            errorLogger = errorLogger
        )

        repository.getByType(isIncome = false)
        val refreshed = async { background.runAll() }
        requested.await()
        harness.vault.wipe()
        response.complete(Response.success(listOf(categoryDto())))
        refreshed.await()

        assertThat(holder.withDatabase { it.categoryDao().getById(FOREIGN_CATEGORY) }).isNull()
    }

    @Test
    fun `transactions answered after the wipe stay out of the new database`() = wipeTest {
        val response = CompletableDeferred<Response<List<TransactionDto>>>()
        val manager = transactionManager(response)

        val pulled = async { manager.pullServerData() }
        requested.await()
        harness.vault.wipe()
        response.complete(Response.success(listOf(transactionDto(seededCategoryId()))))

        assertThat(pulled.await()).isFalse()
        assertThat(holder.withDatabase { it.transactionDao().getByServerId(SERVER_TX) }).isNull()
    }

    @Test
    fun `account pull started by the repository is bound on its own`() = wipeTest {
        // Так его зовёт TransactionRepositoryImpl.getByAccountAndPeriod — без внешнего pullServerData
        val response = CompletableDeferred<Response<List<TransactionDto>>>()
        val manager = transactionManager(response)

        val pulled = async {
            manager.pullFromRemoteForAccount("local-account", startDate = TIME, endDate = TIME)
        }
        requested.await()
        harness.vault.wipe()
        response.complete(Response.success(listOf(transactionDto(seededCategoryId()))))

        assertThat(pulled.await()).isFalse()
        assertThat(holder.withDatabase { it.transactionDao().getByServerId(SERVER_TX) }).isNull()
    }

    /** Pull операций по синхронизированному счёту, которому сервер отвечает [response]. */
    private suspend fun transactionManager(
        response: CompletableDeferred<Response<List<TransactionDto>>>
    ): TransactionSyncManagerImpl {
        accounts.create(syncedAccount())
        val remote = mockk<TransactionRemoteDataSource> {
            coEvery { getByAccountAndPeriod(any(), any(), any()) } coAnswers { awaitAnswer(response) }
        }
        return TransactionSyncManagerImpl(
            remote,
            transactions,
            accounts,
            categories,
            holder,
            errorLogger
        )
    }

    /** Засеянные категории есть в любой базе, и в новой тоже: операция с ней прошла бы слияние. */
    private suspend fun seededCategoryId() =
        holder.withDatabase { it.categoryDao().getAll().first() }.first().id

    @Test
    fun `outbox success after the wipe does not settle the next user's entry`() = wipeTest {
        // Сервер принял операцию прошлого пользователя — обработчик докладывает об отправке
        outboxAnsweredAfterWipe(OutboxSendResult.Success())
    }

    @Test
    fun `outbox rejection after the wipe is not reported as a dead letter`() = wipeTest {
        outboxAnsweredAfterWipe(OutboxSendResult.Terminal("HTTP 400"))
    }

    @Test
    fun `outbox transient failure on the last attempt after the wipe is not reported`() = wipeTest {
        // На последней попытке временный сбой уводит запись в dead-letter
        outboxAnsweredAfterWipe(OutboxSendResult.Transient("HTTP 503"))
    }

    /**
     * Запись прошлого пользователя в полёте, стирание, у следующего пользователя запись с тем же
     * номером, затем ответ сервера [answer].
     */
    private suspend fun TestScope.outboxAnsweredAfterWipe(answer: OutboxSendResult) {
        // Номера очереди — autoincrement: в новой базе они начинаются заново и совпадают со старыми.
        // Попытка последняя: сбой обработки увёл бы запись в dead-letter с отчётом в Crashlytics
        val lastAttempt = OutboxRetryPolicy.MAX_ATTEMPTS - 1
        assertThat(outbox.enqueue(outboxEntry("previous-user", lastAttempt))).isEqualTo(1L)
        val response = CompletableDeferred<OutboxSendResult>()
        val sender = mockk<OutboxSender> {
            coEvery { send(any()) } coAnswers { awaitAnswer(response) }
        }
        val processor = OutboxProcessor(outbox, realHandler(sender), holder, Clock.systemUTC())

        val processed = async { runCatching { processor.process() } }
        requested.await()
        harness.vault.wipe()
        assertThat(outbox.enqueue(outboxEntry("next-user"))).isEqualTo(1L)
        response.complete(answer)

        // Разбор прерывается, как на закрытой базе, — вызывающие это уже обрабатывают
        assertThat(processed.await().exceptionOrNull()).isInstanceOf(StaleDataException::class.java)
        // Операция нового пользователя не помечена отправленной, не уведена в dead-letter и не
        // удалена из очереди
        assertThat(outbox.countUnsent()).isEqualTo(1)
        // Стёртая запись — не «сбой обработки» и не dead-letter
        verify(exactly = 0) { errorLogger.recordError(any<String>()) }
    }

    /** Настоящий обработчик очереди поверх [holder]; сеть — [sender]. */
    private fun realHandler(sender: OutboxSender) = OutboxEntryHandler(
        localDataSource = outbox,
        sender = sender,
        retryPolicy = OutboxRetryPolicy(),
        clock = Clock.systemUTC(),
        errorLogger = errorLogger,
        transactionRunner = RoomTransactionRunner(holder, noopContext)
    )

    private val noopContext = object : AppCoroutineContext {
        override fun launch(block: suspend CoroutineScope.() -> Unit) = Unit
        override suspend fun launchSync(block: suspend () -> Unit) = Unit
    }

    private suspend fun <T> awaitAnswer(response: CompletableDeferred<T>): T {
        requested.complete(Unit)
        return response.await()
    }

    private fun wipeTest(body: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            harness.vault.initialize()
            body()
        } finally {
            harness.release()
        }
    }

    private fun accountDto() = AccountDto(
        id = "server-account",
        userId = "previous-user",
        name = "Счёт прошлого пользователя",
        balance = BigDecimal("100"),
        currencyId = "RUB",
        createdAt = TIME,
        updatedAt = TIME
    )

    private fun categoryDto() = CategoryDto(
        id = FOREIGN_CATEGORY,
        createdAt = TIME,
        updatedAt = TIME,
        name = "Категория прошлого пользователя",
        emoji = "💼",
        isIncome = false
    )

    private fun transactionDto(categoryId: String) = TransactionDto(
        id = SERVER_TX,
        createdAt = TIME,
        updatedAt = TIME,
        accountId = "server-account",
        categoryId = categoryId,
        amount = BigDecimal("10"),
        dateTime = TIME,
        comment = null
    )

    private fun syncedAccount() = AccountEntity(
        localId = "local-account",
        serverId = "server-account",
        name = "Счёт",
        balance = "100",
        currencyId = "RUB",
        createdAt = TIME,
        updatedAt = TIME,
        syncStatus = SyncStatus.SYNCED
    )

    private fun outboxEntry(owner: String, attempts: Int = 0) = OutboxEntryEntity(
        entityType = OutboxEntityType.ACCOUNT,
        entityLocalId = owner,
        dependencyKey = owner,
        operation = OutboxOperation.CREATE,
        targetServerId = null,
        payload = """{"id":"$owner"}""",
        idempotencyKey = "op-$owner",
        status = OutboxStatus.PENDING,
        attemptCount = attempts,
        nextAttemptAt = 0,
        lastError = null,
        createdAt = NOW,
        updatedAt = NOW
    )

    private companion object {
        const val TIME = "2026-09-01T12:00:00Z"
        const val NOW = 1_788_000_000_000L
        const val SERVER_TX = "server-transaction"
        const val FOREIGN_CATEGORY = "category-of-previous-user"
    }
}
