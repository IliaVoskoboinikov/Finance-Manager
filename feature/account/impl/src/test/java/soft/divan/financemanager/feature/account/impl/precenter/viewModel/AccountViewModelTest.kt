package soft.divan.financemanager.feature.account.impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.account.impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.HasAccountTransactionsUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.impl.navigation.ACCOUNT_ID_KEY
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountEvent
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountMode
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUiState
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Тесты [AccountViewModel].
 *
 * `uiState` собирается через `onStart { loadAccount() }` + `stateIn(WhileSubscribed)`, поэтому
 * загрузка стартует только при наличии подписчика — тесты подписываются хелпером [subscribe]
 * и отменяют коллектор в конце. `Dispatchers.Main` подменяется на [UnconfinedTestDispatcher]
 * в [setUp]/[tearDown]. Одноразовые эффекты (навигация/тосты) проверяются через `eventFlow`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val createAccountUseCase = mockk<CreateAccountUseCase>()
    private val updateAccountUseCase = mockk<UpdateAccountUseCase>()
    private val getAccountByIdUseCase = mockk<GetAccountByIdUseCase>()
    private val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
    private val hasAccountTransactionsUseCase = mockk<HasAccountTransactionsUseCase>()
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)

    private val domainAccount = Account(
        id = "local-1",
        name = "Cash",
        balance = BigDecimal("100.50"),
        currencyId = "rub-id",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { hasAccountTransactionsUseCase(any()) } returns DomainResult.Success(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createModeViewModel() = AccountViewModel(
        createAccountUseCase = createAccountUseCase,
        updateAccountUseCase = updateAccountUseCase,
        getAccountByIdUseCase = getAccountByIdUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
        hasAccountTransactionsUseCase = hasAccountTransactionsUseCase,
        hapticsManager = hapticsManager,
        savedStateHandle = SavedStateHandle()
    )

    private fun editModeViewModel(id: String = "local-1") = AccountViewModel(
        createAccountUseCase = createAccountUseCase,
        updateAccountUseCase = updateAccountUseCase,
        getAccountByIdUseCase = getAccountByIdUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
        hasAccountTransactionsUseCase = hasAccountTransactionsUseCase,
        hapticsManager = hapticsManager,
        savedStateHandle = SavedStateHandle(mapOf(ACCOUNT_ID_KEY to id))
    )

    /** Подписка на uiState, чтобы сработал onStart { loadAccount() }. */
    private fun CoroutineScope.subscribe(vm: AccountViewModel): Job =
        launch(UnconfinedTestDispatcher()) { vm.uiState.collect {} }

    private fun successState(vm: AccountViewModel) =
        vm.uiState.value as AccountUiState.Success

    /* ---------- create mode ---------- */

    @Test
    fun `create mode starts with blank account and RUB currency`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        val state = successState(vm)
        assertThat(state.mode).isEqualTo(AccountMode.Create)
        assertThat(state.account.name).isEmpty()
        assertThat(state.account.balance).isEqualTo("0")
        assertThat(state.account.currencyId).isEqualTo(CurrencySymbol.RUB.id)
        assertThat(UUID.fromString(state.account.id)).isNotNull()

        job.cancel()
    }

    @Test
    fun `createAccount in create mode saves and emits Saved with success haptic`() = runTest {
        coEvery { createAccountUseCase(any()) } returns DomainResult.Success(Unit)
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<AccountEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.updateName("New account")
        vm.createAccount()

        coVerify(exactly = 1) { createAccountUseCase(match { it.name == "New account" }) }
        verify(exactly = 1) { hapticsManager.perform(HapticType.SUCCESS) }
        assertThat(events).containsExactly(AccountEvent.Saved)

        job.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `createAccount failure emits ShowError with error haptic and restores state`() = runTest {
        coEvery { createAccountUseCase(any()) } returns
            DomainResult.Failure(DomainError.NetworkUnavailable)
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<AccountEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.createAccount()

        verify(exactly = 1) { hapticsManager.perform(HapticType.ERROR) }
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(AccountEvent.ShowError::class.java)
        assertThat(vm.uiState.value).isInstanceOf(AccountUiState.Success::class.java)

        job.cancel()
        eventsJob.cancel()
    }

    /* ---------- edit mode ---------- */

    @Test
    fun `edit mode loads account by id`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        val state = successState(vm)
        assertThat(state.mode).isEqualTo(AccountMode.Edit("local-1"))
        assertThat(state.account.name).isEqualTo("Cash")
        assertThat(state.account.balance).isEqualTo("100.5")

        job.cancel()
    }

    @Test
    fun `edit mode marks hasTransactions true when account has operations`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        coEvery { hasAccountTransactionsUseCase("local-1") } returns DomainResult.Success(true)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        assertThat(successState(vm).hasTransactions).isTrue()

        job.cancel()
    }

    @Test
    fun `edit mode keeps hasTransactions false when check fails`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        coEvery { hasAccountTransactionsUseCase("local-1") } returns
            DomainResult.Failure(DomainError.Unknown(null))
        val vm = editModeViewModel()
        val job = subscribe(vm)

        assertThat(successState(vm).hasTransactions).isFalse()

        job.cancel()
    }

    @Test
    fun `edit mode shows error when account load fails`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns
            DomainResult.Failure(DomainError.NoData)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        assertThat(vm.uiState.value).isInstanceOf(AccountUiState.Error::class.java)

        job.cancel()
    }

    @Test
    fun `createAccount in edit mode calls update use case`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        coEvery { updateAccountUseCase(any()) } returns DomainResult.Success(Unit)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        vm.createAccount()

        coVerify(exactly = 1) { updateAccountUseCase(match { it.id == "local-1" }) }
        coVerify(exactly = 0) { createAccountUseCase(any()) }

        job.cancel()
    }

    /* ---------- input handling ---------- */

    @Test
    fun `updateName changes account name in state`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.updateName("Wallet")

        assertThat(successState(vm).account.name).isEqualTo("Wallet")

        job.cancel()
    }

    @Test
    fun `updateCurrency changes currency in state`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.updateCurrency(CurrencySymbol.USD.id)

        assertThat(successState(vm).account.currencyId).isEqualTo(CurrencySymbol.USD.id)

        job.cancel()
    }

    @Test
    fun `balance input accepts valid money values`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.onBalanceInputChanged("123.45")

        assertThat(successState(vm).account.balance).isEqualTo("123.45")

        job.cancel()
    }

    @Test
    fun `balance input replaces leading zero with typed digit`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.onBalanceInputChanged("05")

        assertThat(successState(vm).account.balance).isEqualTo("5")

        job.cancel()
    }

    @Test
    fun `empty balance input resets to zero`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.onBalanceInputChanged("123.45")
        vm.onBalanceInputChanged("")

        assertThat(successState(vm).account.balance).isEqualTo("0")

        job.cancel()
    }

    @Test
    fun `invalid balance input is ignored`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.onBalanceInputChanged("12.3")
        vm.onBalanceInputChanged("abc")
        vm.onBalanceInputChanged("1.234")

        assertThat(successState(vm).account.balance).isEqualTo("12.3")

        job.cancel()
    }

    /* ---------- delete ---------- */

    @Test
    fun `delete in edit mode emits Deleted with success haptic`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        coEvery { deleteAccountUseCase("local-1") } returns DomainResult.Success(Unit)
        val vm = editModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<AccountEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.delete()

        verify(exactly = 1) { hapticsManager.perform(HapticType.SUCCESS) }
        assertThat(events).containsExactly(AccountEvent.Deleted)

        job.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `delete failure emits ShowError with error haptic`() = runTest {
        coEvery { getAccountByIdUseCase("local-1") } returns DomainResult.Success(domainAccount)
        coEvery { deleteAccountUseCase("local-1") } returns
            DomainResult.Failure(DomainError.Unknown(null))
        val vm = editModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<AccountEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.delete()

        verify(exactly = 1) { hapticsManager.perform(HapticType.ERROR) }
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(AccountEvent.ShowError::class.java)

        job.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `delete in create mode does nothing`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.delete()

        coVerify(exactly = 0) { deleteAccountUseCase(any()) }

        job.cancel()
    }
}
