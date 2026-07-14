package soft.divan.financemanager.feature.auth.impl.presenter.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.repository.AuthRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthAction
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthEvent
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUiState
import soft.divan.financemanager.sync.worker.SyncCoordinator

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val authRepository = mockk<AuthRepository>()
    private val getAuthStatusUseCase = mockk<GetAuthStatusUseCase>()
    private val syncCoordinator = mockk<SyncCoordinator>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getAuthStatusUseCase() } returns flowOf(AuthStatus.UNAUTHORIZED)
        coEvery { syncCoordinator.syncAll() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AuthViewModel(
        authRepository = authRepository,
        getAuthStatusUseCase = getAuthStatusUseCase,
        syncCoordinator = syncCoordinator
    )

    private fun success(vm: AuthViewModel) = vm.uiState.value as AuthUiState.Success

    /* ---------- form actions ---------- */

    @Test
    fun `update name and password change form state`() {
        val vm = viewModel()

        vm.onAction(AuthAction.UpdateName("user"))
        vm.onAction(AuthAction.UpdatePassword("secret"))

        assertThat(success(vm).authUi.name).isEqualTo("user")
        assertThat(success(vm).authUi.pass).isEqualTo("secret")
    }

    @Test
    fun `toggle mode switches between login and register`() {
        val vm = viewModel()

        assertThat(success(vm).authUi.isLoginMode).isTrue()
        vm.onAction(AuthAction.ToggleMode)
        assertThat(success(vm).authUi.isLoginMode).isFalse()
    }

    @Test
    fun `dismiss dialogs hides merge and logout dialogs`() {
        val vm = viewModel()
        vm.onAction(AuthAction.OnLogoutClick)
        assertThat(success(vm).showLogoutDialog).isTrue()

        vm.onAction(AuthAction.DismissDialogs)

        assertThat(success(vm).showLogoutDialog).isFalse()
        assertThat(success(vm).showMergeDialog).isFalse()
    }

    /* ---------- auth ---------- */

    @Test
    fun `login success triggers sync and navigates to main`() = runTest {
        coEvery { authRepository.login("user", "secret", shouldMergeData = true) } returns
            DomainResult.Success(Unit)
        val vm = viewModel()
        val events = mutableListOf<AuthEvent>()
        val job = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.onAction(AuthAction.UpdateName("user"))
        vm.onAction(AuthAction.UpdatePassword("secret"))
        vm.onAction(AuthAction.OnAuthClick)

        coVerify(exactly = 1) { syncCoordinator.syncAll() }
        assertThat(events).contains(AuthEvent.NavigateToMain)

        job.cancel()
    }

    @Test
    fun `register is used when login mode is off`() = runTest {
        coEvery { authRepository.register("user", "secret", shouldMergeData = true) } returns
            DomainResult.Success(Unit)
        val vm = viewModel()

        vm.onAction(AuthAction.UpdateName("user"))
        vm.onAction(AuthAction.UpdatePassword("secret"))
        vm.onAction(AuthAction.ToggleMode)
        vm.onAction(AuthAction.OnAuthClick)

        coVerify(exactly = 1) { authRepository.register("user", "secret", shouldMergeData = true) }
    }

    @Test
    fun `guest login success asks about merging data`() = runTest {
        every { getAuthStatusUseCase() } returns flowOf(AuthStatus.GUEST)
        coEvery { authRepository.login(any(), any(), any()) } returns DomainResult.Success(Unit)
        val vm = viewModel()

        vm.onAction(AuthAction.OnAuthClick)

        assertThat(success(vm).showMergeDialog).isTrue()
        coVerify(exactly = 0) { syncCoordinator.syncAll() }
    }

    @Test
    fun `unauthorized error maps to invalid credentials message`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns
            DomainResult.Failure(DomainError.Unauthorized)
        val vm = viewModel()

        vm.onAction(AuthAction.OnAuthClick)

        assertThat(success(vm).errorMessage).isNotNull()
    }

    @Test
    fun `network error and generic error map to their own messages`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns
            DomainResult.Failure(DomainError.NetworkUnavailable)
        val networkVm = viewModel()
        networkVm.onAction(AuthAction.OnAuthClick)
        val networkMessage = success(networkVm).errorMessage

        coEvery { authRepository.login(any(), any(), any()) } returns
            DomainResult.Failure(DomainError.NoData)
        val genericVm = viewModel()
        genericVm.onAction(AuthAction.OnAuthClick)
        val genericMessage = success(genericVm).errorMessage

        assertThat(networkMessage).isNotNull()
        assertThat(genericMessage).isNotNull()
        // разные доменные ошибки дают разные строковые ресурсы
        assertThat(networkMessage).isNotEqualTo(genericMessage)
    }

    @Test
    fun `failed sync still navigates to main with toast`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns DomainResult.Success(Unit)
        coEvery { syncCoordinator.syncAll() } returns false
        val vm = viewModel()
        val events = mutableListOf<AuthEvent>()
        val job = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.onAction(AuthAction.OnAuthClick)

        assertThat(events.filterIsInstance<AuthEvent.ShowToast>()).hasSize(1)
        assertThat(events).contains(AuthEvent.NavigateToMain)

        job.cancel()
    }

    /* ---------- merge confirm ---------- */

    @Test
    fun `merge confirm keeps guest data and syncs`() = runTest {
        val vm = viewModel()

        vm.onAction(AuthAction.OnMergeConfirm(shouldMerge = true))

        coVerify(exactly = 0) { authRepository.clearUserData() }
        coVerify(exactly = 1) { syncCoordinator.syncAll() }
    }

    @Test
    fun `merge decline clears guest data before sync`() = runTest {
        coEvery { authRepository.clearUserData() } returns DomainResult.Success(Unit)
        val vm = viewModel()

        vm.onAction(AuthAction.OnMergeConfirm(shouldMerge = false))

        coVerify(exactly = 1) { authRepository.clearUserData() }
        coVerify(exactly = 1) { syncCoordinator.syncAll() }
    }

    /* ---------- logout / guest ---------- */

    @Test
    fun `logout success resets state to empty form`() = runTest {
        coEvery { authRepository.logout(true) } returns DomainResult.Success(Unit)
        val vm = viewModel()
        vm.onAction(AuthAction.UpdateName("user"))

        vm.onAction(AuthAction.OnLogoutConfirm(shouldClear = true))

        coVerify(exactly = 1) { authRepository.logout(true) }
        assertThat(success(vm).authUi.name).isEmpty()
    }

    @Test
    fun `logout failure restores previous state`() = runTest {
        coEvery { authRepository.logout(false) } returns
            DomainResult.Failure(DomainError.Unknown(null))
        val vm = viewModel()
        vm.onAction(AuthAction.UpdateName("user"))

        vm.onAction(AuthAction.OnLogoutConfirm(shouldClear = false))

        assertThat(success(vm).authUi.name).isEqualTo("user")
    }

    @Test
    fun `guest click navigates to main on success`() = runTest {
        coEvery { authRepository.loginAsGuest() } returns DomainResult.Success(Unit)
        val vm = viewModel()
        val events = mutableListOf<AuthEvent>()
        val job = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.onAction(AuthAction.OnGuestClick)

        assertThat(events).containsExactly(AuthEvent.NavigateToMain)

        job.cancel()
    }

    @Test
    fun `guest click failure is silent`() = runTest {
        coEvery { authRepository.loginAsGuest() } returns
            DomainResult.Failure(DomainError.NetworkUnavailable)
        val vm = viewModel()
        val events = mutableListOf<AuthEvent>()
        val job = launch(UnconfinedTestDispatcher()) { vm.eventFlow.collect { events += it } }

        vm.onAction(AuthAction.OnGuestClick)

        assertThat(events).isEmpty()

        job.cancel()
    }
}
