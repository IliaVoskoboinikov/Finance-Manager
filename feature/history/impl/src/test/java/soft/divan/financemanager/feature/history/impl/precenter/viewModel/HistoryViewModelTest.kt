package soft.divan.financemanager.feature.history.impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
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
import soft.divan.financemanager.feature.history.impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.history.impl.precenter.model.HistoryUiState
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val getTransactionsByPeriodUseCase = mockk<GetTransactionsByPeriodUseCase>()
    private val getSumTransactionsUseCase = mockk<GetSumTransactionsUseCase>()

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
        amount = BigDecimal("100"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.EPOCH,
        comment = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(isIncome: Boolean = false) = HistoryViewModel(
        getTransactionsByPeriodUseCase = getTransactionsByPeriodUseCase,
        getSumTransactionsUseCase = getSumTransactionsUseCase,
        ioDispatcher = UnconfinedTestDispatcher(),
        savedStateHandle = SavedStateHandle(mapOf(IS_INCOME_KEY to isIncome))
    )

    private fun stubSuccess(transactions: List<Transaction>) {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Success(Triple(transactions, CurrencySymbol.RUB, listOf(category))))
        every { getSumTransactionsUseCase(transactions) } returns
            transactions.sumOf { it.amount }
    }

    @Test
    fun `success maps transactions and formats sum`() = runTest {
        stubSuccess(listOf(transaction("t1")))
        val vm = viewModel()

        val state = vm.uiState.first { it !is HistoryUiState.Loading }

        val success = state as HistoryUiState.Success
        assertThat(success.transactions.map { it.id }).containsExactly("t1")
        assertThat(success.sumTransaction).isEqualTo("100 ₽")
    }

    @Test
    fun `transactions with unknown category are skipped in ui list`() = runTest {
        stubSuccess(listOf(transaction("t1"), transaction("t2", categoryId = "ghost")))
        val vm = viewModel()

        val state = vm.uiState.first { it !is HistoryUiState.Loading }

        val success = state as HistoryUiState.Success
        assertThat(success.transactions.map { it.id }).containsExactly("t1")
    }

    @Test
    fun `empty transactions produce EmptyData`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()

        val state = vm.uiState.first { it !is HistoryUiState.Loading }

        assertThat(state).isEqualTo(HistoryUiState.EmptyData)
    }

    @Test
    fun `failure produces Error state`() = runTest {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))
        val vm = viewModel()

        val state = vm.uiState.first { it !is HistoryUiState.Loading }

        assertThat(state).isInstanceOf(HistoryUiState.Error::class.java)
    }

    @Test
    fun `isIncome flag is passed to use case`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel(isIncome = true)

        vm.uiState.first { it !is HistoryUiState.Loading }

        verify { getTransactionsByPeriodUseCase(isIncome = true, period = any()) }
    }

    @Test
    fun `date updates are exposed and reload keeps dates`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()
        vm.uiState.first { it !is HistoryUiState.Loading }

        vm.updateStartDate(LocalDate.of(2024, 1, 1))
        vm.updateEndDate(LocalDate.of(2024, 1, 31))
        vm.reloadData()

        assertThat(vm.startDate.value).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(vm.endDate.value).isEqualTo(LocalDate.of(2024, 1, 31))
        // перезагрузка уходит через flowOn(IO) — ждём вызов, а не мгновенное состояние
        verify(timeout = 5000, atLeast = 2) { getTransactionsByPeriodUseCase(any(), any()) }
    }
}
