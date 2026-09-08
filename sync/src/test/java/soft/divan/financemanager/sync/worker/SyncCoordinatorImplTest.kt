package soft.divan.financemanager.sync.worker

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.sync.domain.usecase.SetLastSyncTimeUseCase

class SyncCoordinatorImplTest {

    private val categorySyncManager = mockk<CategorySyncManager>()
    private val accountSyncManager = mockk<AccountSyncManager>()
    private val transactionSyncManager = mockk<TransactionSyncManager>()
    private val outboxProcessor = mockk<OutboxProcessor>(relaxed = true)
    private val setLastSyncTimeUseCase = mockk<SetLastSyncTimeUseCase>(relaxed = true)

    private val coordinator = SyncCoordinatorImpl(
        categorySyncManager = categorySyncManager,
        accountSyncManager = accountSyncManager,
        transactionSyncManager = transactionSyncManager,
        outboxProcessor = outboxProcessor,
        setLastSyncTimeUseCase = setLastSyncTimeUseCase
    )

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun stubManagers(
        category: Boolean = true,
        account: Boolean = true,
        transaction: Boolean = true
    ) {
        coEvery { categorySyncManager.syncWith(any()) } returns category
        coEvery { accountSyncManager.syncWith(any()) } returns account
        coEvery { transactionSyncManager.syncWith(any()) } returns transaction
    }

    @Test
    fun `syncAll runs managers in category account transaction order`() = runTest {
        stubManagers()

        val result = coordinator.syncAll()

        assertThat(result).isTrue()
        coVerifyOrder {
            categorySyncManager.syncWith(coordinator)
            accountSyncManager.syncWith(coordinator)
            transactionSyncManager.syncWith(coordinator)
        }
    }

    @Test
    fun `syncAll stores last sync time on success`() = runTest {
        stubManagers()

        coordinator.syncAll()

        coVerify(exactly = 1) { setLastSyncTimeUseCase(more(0L)) }
    }

    @Test
    fun `syncAll keeps pulling the rest when category sync fails`() = runTest {
        stubManagers(category = false)

        val result = coordinator.syncAll()

        // Сбой чтения категорий не повод оставить счета и транзакции без обновления
        assertThat(result).isFalse()
        coVerify(exactly = 1) { accountSyncManager.syncWith(any()) }
        coVerify(exactly = 1) { transactionSyncManager.syncWith(any()) }
        coVerify(exactly = 0) { setLastSyncTimeUseCase(any()) }
    }

    @Test
    fun `syncAll keeps pulling transactions when account sync fails`() = runTest {
        stubManagers(account = false)

        val result = coordinator.syncAll()

        assertThat(result).isFalse()
        coVerify(exactly = 1) { transactionSyncManager.syncWith(any()) }
        coVerify(exactly = 0) { setLastSyncTimeUseCase(any()) }
    }

    @Test
    fun `syncAll returns false when transaction sync fails`() = runTest {
        stubManagers(transaction = false)

        val result = coordinator.syncAll()

        assertThat(result).isFalse()
        coVerify(exactly = 0) { setLastSyncTimeUseCase(any()) }
    }

    @Test
    fun `syncAll treats manager exception as failed step without stopping`() = runTest {
        stubManagers()
        coEvery { categorySyncManager.syncWith(any()) } throws RuntimeException("boom")

        val result = coordinator.syncAll()

        assertThat(result).isFalse()
        // Исключение в одном менеджере не должно ронять весь цикл синхронизации
        coVerify(exactly = 1) { accountSyncManager.syncWith(any()) }
        coVerify(exactly = 1) { outboxProcessor.process() }
    }

    @Test
    fun `syncAll processes the outbox after a successful pull`() = runTest {
        stubManagers()

        coordinator.syncAll()

        coVerify(exactly = 1) { outboxProcessor.process() }
    }

    @Test
    fun `syncAll processes the outbox even when a pull step fails`() = runTest {
        // Локальные изменения не должны ждать отправки из-за проблем с чтением
        stubManagers(category = false)

        val result = coordinator.syncAll()

        assertThat(result).isFalse()
        coVerify(exactly = 1) { outboxProcessor.process() }
    }

    @Test
    fun `syncAll returns false when outbox processing fails`() = runTest {
        stubManagers()
        coEvery { outboxProcessor.process() } throws RuntimeException("boom")

        val result = coordinator.syncAll()

        assertThat(result).isFalse()
        coVerify(exactly = 0) { setLastSyncTimeUseCase(any()) }
    }
}
