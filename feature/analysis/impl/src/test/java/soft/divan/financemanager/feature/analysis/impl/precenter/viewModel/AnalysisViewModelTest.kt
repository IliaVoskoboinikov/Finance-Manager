package soft.divan.financemanager.feature.analysis.impl.precenter.viewModel

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
import soft.divan.financemanager.feature.analysis.impl.domain.usecase.GetCategoryPieChartDataUseCase
import soft.divan.financemanager.feature.analysis.impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.analysis.impl.precenter.model.AnalysisUiState
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    private val getSumTransactionsUseCase = mockk<GetSumTransactionsUseCase>()
    private val getTransactionsByPeriodUseCase = mockk<GetTransactionsByPeriodUseCase>()
    private val getCategoryPieChartDataUseCase = mockk<GetCategoryPieChartDataUseCase>()

    private val category = Category(
        id = "cat-1",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    private val transaction = Transaction(
        id = "t1",
        accountLocalId = "a1",
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "cat-1",
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

    private fun viewModel(isIncome: Boolean? = false) = AnalysisViewModel(
        getSumTransactionsUseCase = getSumTransactionsUseCase,
        getTransactionsByPeriodUseCase = getTransactionsByPeriodUseCase,
        getCategoryPieChartDataUseCase = getCategoryPieChartDataUseCase,
        ioDispatcher = UnconfinedTestDispatcher(),
        savedStateHandle = isIncome?.let { SavedStateHandle(mapOf(IS_INCOME_KEY to it)) }
            ?: SavedStateHandle()
    )

    private fun stubSuccess(transactions: List<Transaction>) {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Success(Triple(transactions, CurrencySymbol.RUB, listOf(category))))
        every { getSumTransactionsUseCase(transactions) } returns
            transactions.sumOf { it.amount }
        every { getCategoryPieChartDataUseCase(transactions, listOf(category)) } returns emptyList()
    }

    @Test
    fun `success state carries formatted sum with currency symbol`() = runTest {
        stubSuccess(listOf(transaction))
        val vm = viewModel()

        val state = vm.uiState.first { it !is AnalysisUiState.Loading }

        val success = state as AnalysisUiState.Success
        assertThat(success.sumTransaction).isEqualTo("100 ₽")
    }

    @Test
    fun `empty transactions produce EmptyData state`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()

        val state = vm.uiState.first { it !is AnalysisUiState.Loading }

        assertThat(state).isEqualTo(AnalysisUiState.EmptyData)
    }

    @Test
    fun `failure produces Error state`() = runTest {
        every { getTransactionsByPeriodUseCase(any(), any()) } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))
        val vm = viewModel()

        val state = vm.uiState.first { it !is AnalysisUiState.Loading }

        assertThat(state).isInstanceOf(AnalysisUiState.Error::class.java)
    }

    @Test
    fun `isIncome flag from navigation is passed to use case`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel(isIncome = true)

        vm.uiState.first { it !is AnalysisUiState.Loading }

        verify { getTransactionsByPeriodUseCase(isIncome = true, period = any()) }
    }

    @Test
    fun `missing isIncome argument defaults to false`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel(isIncome = null)

        vm.uiState.first { it !is AnalysisUiState.Loading }

        verify { getTransactionsByPeriodUseCase(isIncome = false, period = any()) }
    }

    @Test
    fun `default period is from first day of month to today`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()

        assertThat(vm.startDate.value).isEqualTo(LocalDate.now().withDayOfMonth(1))
        assertThat(vm.endDate.value).isEqualTo(LocalDate.now())
    }

    @Test
    fun `updateStartDate and updateEndDate change exposed dates`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()

        vm.updateStartDate(LocalDate.of(2024, 1, 1))
        vm.updateEndDate(LocalDate.of(2024, 1, 31))

        assertThat(vm.startDate.value).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(vm.endDate.value).isEqualTo(LocalDate.of(2024, 1, 31))
    }

    @Test
    fun `reloadData triggers a new load with the same dates`() = runTest {
        stubSuccess(emptyList())
        val vm = viewModel()
        vm.uiState.first { it !is AnalysisUiState.Loading }

        vm.reloadData()

        // перезагрузка уходит через flowOn(IO) — ждём вызов, а не мгновенное состояние
        verify(timeout = 5000, atLeast = 2) { getTransactionsByPeriodUseCase(any(), any()) }
    }
}
