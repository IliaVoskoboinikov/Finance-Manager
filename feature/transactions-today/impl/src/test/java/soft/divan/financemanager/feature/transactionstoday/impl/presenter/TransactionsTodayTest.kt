package soft.divan.financemanager.feature.transactionstoday.impl.presenter

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.mapper.toUi
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.TransactionsTodayUiState
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.viewmodel.TransactionsTodayViewModel
import java.math.BigDecimal
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsTodayTest {

    private val category = Category(
        id = "cat-1",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    private fun transaction(id: String, categoryId: String = "cat-1") = Transaction(
        id = id,
        accountLocalId = "a1",
        targetAccountLocalId = null,
        currencyId = CurrencySymbol.RUB.id,
        categoryId = categoryId,
        amount = BigDecimal("50.00"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse("2024-03-07T12:30:00Z"),
        comment = "lunch",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    /* ---------- mappers ---------- */

    @Test
    fun `Category toUi keeps name and emoji`() {
        val ui = category.toUi()

        assertThat(ui.name).isEqualTo("Food")
        assertThat(ui.emoji).isEqualTo("🍔")
    }

    @Test
    fun `Transaction toUi formats amount date and category`() {
        val ui = transaction("t1").toUi(category)

        assertThat(ui.id).isEqualTo("t1")
        assertThat(ui.amountFormatted).isEqualTo("50 ₽")
        assertThat(ui.category.name).isEqualTo("Food")
        assertThat(ui.transactionDateTime).isEqualTo(
            UiDateFormatter.formatDateTime(Instant.parse("2024-03-07T12:30:00Z"))
        )
        assertThat(ui.comment).isEqualTo("lunch")
    }

    /* ---------- view model ---------- */

    private val getTransactionsByPeriodUseCase = mockk<GetTransactionsByPeriodUseCase>()
    private val getSumTransactionsUseCase = mockk<GetSumTransactionsUseCase>()
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TransactionsTodayViewModel(
        getTransactionsByPeriodUseCase = getTransactionsByPeriodUseCase,
        getSumTransactionsUseCase = getSumTransactionsUseCase,
        hapticsManager = hapticsManager,
        ioDispatcher = UnconfinedTestDispatcher()
    )

    private fun stubSuccess(transactions: List<Transaction>) {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Success(Triple(transactions, CurrencySymbol.RUB, listOf(category))))
        every { getSumTransactionsUseCase(transactions) } returns
            transactions.sumOf { it.amount }
    }

    @Test
    fun `load publishes Success with mapped transactions and sum`() = runTest {
        stubSuccess(listOf(transaction("t1")))
        val vm = viewModel()

        vm.loadTodayTransactions(isIncome = false)
        val state = vm.uiState.first { it !is TransactionsTodayUiState.Loading }

        val success = state as TransactionsTodayUiState.Success
        assertThat(success.transactions.map { it.id }).containsExactly("t1")
        assertThat(success.sumTransaction).isEqualTo("50.00 ₽")
    }

    @Test
    fun `transactions with unknown category are skipped`() = runTest {
        stubSuccess(listOf(transaction("t1"), transaction("t2", categoryId = "ghost")))
        val vm = viewModel()

        vm.loadTodayTransactions(isIncome = false)
        val state = vm.uiState.first { it !is TransactionsTodayUiState.Loading }

        assertThat((state as TransactionsTodayUiState.Success).transactions.map { it.id })
            .containsExactly("t1")
    }

    @Test
    fun `failure publishes Error state`() = runTest {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))
        val vm = viewModel()

        vm.loadTodayTransactions(isIncome = false)
        val state = vm.uiState.first { it !is TransactionsTodayUiState.Loading }

        assertThat(state).isInstanceOf(TransactionsTodayUiState.Error::class.java)
    }

    @Test
    fun `retry repeats load with the last income flag`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()

        vm.loadTodayTransactions(isIncome = true)
        vm.retry()

        verify(exactly = 2) { getTransactionsByPeriodUseCase(isIncome = true, period = any()) }
    }

    @Test
    fun `retry does nothing before the first load`() = runTest {
        val vm = viewModel()

        vm.retry()

        verify(exactly = 0) { getTransactionsByPeriodUseCase(any(), any()) }
    }

    @Test
    fun `hapticNavigation performs click haptic`() = runTest {
        viewModel().hapticNavigation()

        verify(exactly = 1) { hapticsManager.perform(HapticType.CLICK) }
    }
}
