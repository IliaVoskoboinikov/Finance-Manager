package soft.divan.financemanager.feature.transaction.impl.precenter.viewModel

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
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
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.sounds.api.domain.SoundPlayer
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.navigation.IS_INCOME_KEY
import soft.divan.financemanager.feature.transaction.impl.navigation.TRANSACTION_ID_KEY
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionEvent
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUiState
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val createUseCase = mockk<CreateTransactionAndUpdateAccountUseCase>()
    private val getAccountsUseCase = mockk<GetAccountsUseCase>()
    private val getTransactionUseCase = mockk<GetTransactionUseCase>()
    private val getCategoriesUseCase = mockk<GetCategoriesByTypeUseCase>()
    private val updateUseCase = mockk<UpdateTransactionAndUpdateAccountUseCase>()
    private val deleteUseCase = mockk<DeleteTransactionAndUpdateAccountUseCase>()
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)
    private val soundPlayer = mockk<SoundPlayer>(relaxUnitFun = true)

    private val account = Account(
        id = "a1",
        name = "Cash",
        balance = BigDecimal("100"),
        currencyId = CurrencySymbol.RUB.id,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    private val category = Category(
        id = "cat-1",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    private val domainTransaction = Transaction(
        id = "t1",
        accountLocalId = "a1",
        targetAccountLocalId = null,
        currencyId = CurrencySymbol.RUB.id,
        categoryId = "cat-1",
        amount = BigDecimal("42"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse("2024-03-07T12:30:00Z"),
        comment = "lunch",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getCategoriesUseCase(any()) } returns
            flowOf(DomainResult.Success(listOf(category)))
        every { getAccountsUseCase() } returns flowOf(DomainResult.Success(listOf(account)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createModeViewModel() = TransactionViewModel(
        createTransactionAndUpdateAccountUseCase = createUseCase,
        getAccountsUseCase = getAccountsUseCase,
        getTransactionUseCase = getTransactionUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        updateTransactionAndUpdateAccountUseCase = updateUseCase,
        deleteTransactionAndUpdateAccountUseCase = deleteUseCase,
        hapticsManager = hapticsManager,
        soundPlayer = soundPlayer,
        savedStateHandle = SavedStateHandle(mapOf(IS_INCOME_KEY to false))
    )

    private fun editModeViewModel(id: String = "t1") = TransactionViewModel(
        createTransactionAndUpdateAccountUseCase = createUseCase,
        getAccountsUseCase = getAccountsUseCase,
        getTransactionUseCase = getTransactionUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        updateTransactionAndUpdateAccountUseCase = updateUseCase,
        deleteTransactionAndUpdateAccountUseCase = deleteUseCase,
        hapticsManager = hapticsManager,
        soundPlayer = soundPlayer,
        savedStateHandle = SavedStateHandle(
            mapOf(TRANSACTION_ID_KEY to id, IS_INCOME_KEY to false)
        )
    )

    private fun TestScope.subscribe(vm: TransactionViewModel): Job =
        launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

    private fun success(vm: TransactionViewModel) =
        vm.uiState.value as TransactionUiState.Success

    /* ---------- load ---------- */

    @Test
    fun `create mode initializes blank transaction from first account and category`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        val state = success(vm)
        assertThat(state.transaction.accountId).isEqualTo("a1")
        assertThat(state.transaction.category.id).isEqualTo("cat-1")
        assertThat(state.transaction.amount).isEqualTo("0")
        assertThat(state.transaction.mode).isEqualTo(TransactionMode.Create)
        assertThat(state.transaction.type).isEqualTo(TransactionType.EXPENSE)

        job.cancel()
    }

    @Test
    fun `edit mode loads transaction by id`() = runTest {
        coEvery { getTransactionUseCase("t1") } returns DomainResult.Success(domainTransaction)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        val state = success(vm)
        assertThat(state.transaction.id).isEqualTo("t1")
        assertThat(state.transaction.amount).isEqualTo("42")
        assertThat(state.transaction.mode).isEqualTo(TransactionMode.Edit("t1"))

        job.cancel()
    }

    @Test
    fun `load failure of categories produces Error state`() = runTest {
        every { getCategoriesUseCase(any()) } returns
            flowOf(DomainResult.Failure(DomainError.NetworkUnavailable))
        val vm = createModeViewModel()
        val job = subscribe(vm)

        assertThat(vm.uiState.value).isInstanceOf(TransactionUiState.Error::class.java)

        job.cancel()
    }

    @Test
    fun `edit mode load failure produces Error state`() = runTest {
        coEvery { getTransactionUseCase("t1") } returns DomainResult.Failure(DomainError.NoData)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        assertThat(vm.uiState.value).isInstanceOf(TransactionUiState.Error::class.java)

        job.cancel()
    }

    /* ---------- input ---------- */

    @Test
    fun `input updates modify transaction state`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.updateComment("dinner")
        vm.updateDate(LocalDate.of(2024, 3, 7))
        vm.updateTime(LocalTime.of(12, 30))
        vm.onAmountInputChanged("55.5")

        val transaction = success(vm).transaction
        assertThat(transaction.comment).isEqualTo("dinner")
        assertThat(transaction.date).isEqualTo("07.03.2024")
        assertThat(transaction.time).isEqualTo("12:30")
        assertThat(transaction.amount).isEqualTo("55.5")

        job.cancel()
    }

    @Test
    fun `invalid amount input is ignored`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)

        vm.onAmountInputChanged("12.3")
        vm.onAmountInputChanged("abc")
        vm.onAmountInputChanged("1.234")

        assertThat(success(vm).transaction.amount).isEqualTo("12.3")

        job.cancel()
    }

    @Test
    fun `updateAccount changes account and currency`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val newAccount = soft.divan.financemanager.feature.transaction.impl.precenter.model
            .AccountUi(id = "a2", name = "Card", balance = "0", currencyId = "usd-id")

        vm.updateAccount(newAccount)

        assertThat(success(vm).transaction.accountId).isEqualTo("a2")
        assertThat(success(vm).transaction.currencyId).isEqualTo("usd-id")

        job.cancel()
    }

    @Test
    fun `updateCategory changes the selected category`() = runTest {
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val newCategory = soft.divan.financemanager.feature.transaction.impl.precenter.model
            .CategoryUi(id = "cat-2", name = "Taxi", emoji = "🚕")

        vm.updateCategory(newCategory)

        assertThat(success(vm).transaction.category).isEqualTo(newCategory)

        job.cancel()
    }

    /* ---------- save / delete ---------- */

    @Test
    fun `save in create mode plays coin sound and emits Saved`() = runTest {
        coEvery { createUseCase(any()) } returns DomainResult.Success(Unit)
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<TransactionEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.eventFlow.collect { events += it }
        }

        vm.save()

        coVerify(exactly = 1) { createUseCase(any()) }
        verify(exactly = 1) { hapticsManager.perform(HapticType.SUCCESS) }
        verify(exactly = 1) { soundPlayer.play(SoundType.COIN) }
        assertThat(events).containsExactly(TransactionEvent.TransactionSaved)

        job.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `save in edit mode uses update use case`() = runTest {
        coEvery { getTransactionUseCase("t1") } returns DomainResult.Success(domainTransaction)
        coEvery { updateUseCase(any()) } returns DomainResult.Success(Unit)
        val vm = editModeViewModel()
        val job = subscribe(vm)

        vm.save()

        coVerify(exactly = 1) { updateUseCase(match { it.id == "t1" }) }
        coVerify(exactly = 0) { createUseCase(any()) }

        job.cancel()
    }

    @Test
    fun `save failure emits ShowError with error haptic and no sound`() = runTest {
        coEvery { createUseCase(any()) } returns DomainResult.Failure(DomainError.Unknown(null))
        val vm = createModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<TransactionEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.eventFlow.collect { events += it }
        }

        vm.save()

        verify(exactly = 1) { hapticsManager.perform(HapticType.ERROR) }
        verify(exactly = 0) { soundPlayer.play(any()) }
        assertThat(events.single()).isInstanceOf(TransactionEvent.ShowError::class.java)
        assertThat(vm.uiState.value).isInstanceOf(TransactionUiState.Success::class.java)

        job.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `delete works only in edit mode`() = runTest {
        coEvery { getTransactionUseCase("t1") } returns DomainResult.Success(domainTransaction)
        coEvery { deleteUseCase(any()) } returns DomainResult.Success(Unit)
        val editVm = editModeViewModel()
        val editJob = subscribe(editVm)

        editVm.delete()
        coVerify(exactly = 1) { deleteUseCase(match { it.id == "t1" }) }

        val createVm = createModeViewModel()
        val createJob = subscribe(createVm)

        createVm.delete()
        coVerify(exactly = 1) { deleteUseCase(any()) }

        editJob.cancel()
        createJob.cancel()
    }

    @Test
    fun `delete failure emits ShowError and restores state`() = runTest {
        coEvery { getTransactionUseCase("t1") } returns DomainResult.Success(domainTransaction)
        coEvery { deleteUseCase(any()) } returns DomainResult.Failure(DomainError.Unknown(null))
        val vm = editModeViewModel()
        val job = subscribe(vm)
        val events = mutableListOf<TransactionEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.eventFlow.collect { events += it }
        }

        vm.delete()

        verify(exactly = 1) { hapticsManager.perform(HapticType.ERROR) }
        assertThat(events.single()).isInstanceOf(TransactionEvent.ShowError::class.java)
        assertThat(vm.uiState.value).isInstanceOf(TransactionUiState.Success::class.java)

        job.cancel()
        eventsJob.cancel()
    }
}
