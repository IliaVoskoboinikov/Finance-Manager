package soft.divan.financemanager.feature.myaccounts.impl

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.myaccounts.impl.domain.usecase.impl.UpdateCurrencyUseCaseIml
import soft.divan.financemanager.feature.myaccounts.impl.presenter.mapper.toUiModel
import soft.divan.financemanager.feature.myaccounts.impl.presenter.model.MyAccountsUiState
import soft.divan.financemanager.feature.myaccounts.impl.presenter.viewmodel.MyAccountsViewModel
import java.math.BigDecimal
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MyAccountsTest {

    private fun account(id: String, balance: String = "100.50") = Account(
        id = id,
        name = "Cash",
        balance = BigDecimal(balance),
        currencyId = CurrencySymbol.RUB.id,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    /* ---------- UpdateCurrencyUseCaseIml ---------- */

    @Test
    fun `update currency delegates to repository`() = runTest {
        val repository = mockk<CurrencyRepository>(relaxUnitFun = true)

        UpdateCurrencyUseCaseIml(repository)(CurrencySymbol.EUR)

        coVerify(exactly = 1) { repository.updateSelectedCurrency(CurrencySymbol.EUR) }
    }

    /* ---------- AccountPresenterMapper ---------- */

    @Test
    fun `toUiModel formats balance with currency symbol`() {
        val ui = account("a1").toUiModel()

        assertThat(ui.id).isEqualTo("a1")
        assertThat(ui.name).isEqualTo("Cash")
        assertThat(ui.balance).isEqualTo("100.50 ₽")
        assertThat(ui.currency).isEqualTo("₽")
    }

    @Test
    fun `toUiModel keeps unknown currency id as symbol`() {
        val ui = account("a1").copy(currencyId = "custom-id").toUiModel()

        assertThat(ui.balance).isEqualTo("100.50 custom-id")
        assertThat(ui.currency).isEqualTo("custom-id")
    }

    /* ---------- MyAccountsViewModel ---------- */

    private val getAccountsUseCase = mockk<GetAccountsUseCase>()
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MyAccountsViewModel(
        getAccountsUseCase,
        hapticsManager,
        UnconfinedTestDispatcher()
    )

    @Test
    fun `accounts load into Success state`() = runTest {
        every { getAccountsUseCase() } returns
            flowOf(DomainResult.Success(listOf(account("a1"), account("a2"))))
        val vm = viewModel()
        val job = launch(UnconfinedTestDispatcher()) { vm.uiState.collect {} }

        val state = vm.uiState.first { it !is MyAccountsUiState.Loading }

        val success = state as MyAccountsUiState.Success
        assertThat(success.accounts.map { it.id }).containsExactly("a1", "a2")

        job.cancel()
    }

    @Test
    fun `empty account list produces EmptyData`() = runTest {
        every { getAccountsUseCase() } returns flowOf(DomainResult.Success(emptyList()))
        val vm = viewModel()
        val job = launch(UnconfinedTestDispatcher()) { vm.uiState.collect {} }

        val state = vm.uiState.first { it !is MyAccountsUiState.Loading }

        assertThat(state).isEqualTo(MyAccountsUiState.EmptyData)

        job.cancel()
    }

    @Test
    fun `failure produces Error state`() = runTest {
        every { getAccountsUseCase() } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))
        val vm = viewModel()
        val job = launch(UnconfinedTestDispatcher()) { vm.uiState.collect {} }

        val state = vm.uiState.first { it !is MyAccountsUiState.Loading }

        assertThat(state).isInstanceOf(MyAccountsUiState.Error::class.java)

        job.cancel()
    }

    @Test
    fun `hapticNavigation performs click haptic`() = runTest {
        every { getAccountsUseCase() } returns flowOf(DomainResult.Success(emptyList()))

        viewModel().hapticNavigation()

        verify(exactly = 1) { hapticsManager.perform(HapticType.CLICK) }
    }
}
