package soft.divan.financemanager.presenter

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
import soft.divan.financemanager.core.network.util.NetworkMonitor
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val networkMonitor = mockk<NetworkMonitor>()
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(online: Boolean): MainViewModel {
        every { networkMonitor.isOnline } returns flowOf(online)
        return MainViewModel(networkMonitor, hapticsManager, UnconfinedTestDispatcher())
    }

    @Test
    fun `isOffline starts with false before network state arrives`() {
        val vm = viewModel(online = true)

        assertThat(vm.isOffline.value).isFalse()
    }

    @Test
    fun `isOffline emits true when network is lost`() = runTest {
        val vm = viewModel(online = false)

        assertThat(vm.isOffline.first { it }).isTrue()
    }

    @Test
    fun `isOffline stays false when network is available`() = runTest {
        val vm = viewModel(online = true)

        assertThat(vm.isOffline.first()).isFalse()
    }

    @Test
    fun `hapticToggleMenu performs toggle haptic`() {
        val vm = viewModel(online = true)

        vm.hapticToggleMenu()

        verify(exactly = 1) { hapticsManager.perform(HapticType.TOGGLE) }
    }
}
