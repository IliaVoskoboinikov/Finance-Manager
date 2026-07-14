package soft.divan.financemanager.feature.designapp.impl.precenter.viewModel

import androidx.compose.ui.graphics.Color
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetCustomAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetThemeModeUseCase
import soft.divan.financemanager.feature.designapp.impl.precenter.model.DesignUiState
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.uikit.theme.AccentColor

@OptIn(ExperimentalCoroutinesApi::class)
class DesignAppViewModelTest {

    private val getThemeModeUseCase = mockk<GetThemeModeUseCase>()
    private val setThemeModeUseCase = mockk<SetThemeModeUseCase>(relaxUnitFun = true)
    private val getAccentColorUseCase = mockk<GetAccentColorUseCase>()
    private val setAccentColorUseCase = mockk<SetAccentColorUseCase>(relaxUnitFun = true)
    private val setCustomAccentColorUseCase =
        mockk<SetCustomAccentColorUseCase>(relaxUnitFun = true)
    private val hapticsManager = mockk<HapticsManager>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getThemeModeUseCase() } returns flowOf(ThemeMode.DARK)
        every { getAccentColorUseCase() } returns flowOf(AccentColor.MINT)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DesignAppViewModel(
        getThemeModeUseCase = getThemeModeUseCase,
        setThemeModeUseCase = setThemeModeUseCase,
        getAccentColorUseCase = getAccentColorUseCase,
        setAccentColorUseCase = setAccentColorUseCase,
        setCustomAccentColorUseCase = setCustomAccentColorUseCase,
        hapticsManager = hapticsManager,
        ioDispatcher = UnconfinedTestDispatcher()
    )

    @Test
    fun `load publishes Success with theme and accent`() = runTest {
        val vm = viewModel()

        val state = vm.uiState.first { it !is DesignUiState.Loading }

        assertThat(state).isEqualTo(
            DesignUiState.Success(themeMode = ThemeMode.DARK, accentColor = AccentColor.MINT)
        )
    }

    @Test
    fun `load publishes Error when settings flow fails`() = runTest {
        every { getThemeModeUseCase() } returns flow { throw IllegalStateException("boom") }

        val vm = viewModel()

        val state = vm.uiState.first { it !is DesignUiState.Loading }
        assertThat(state).isInstanceOf(DesignUiState.Error::class.java)
    }

    @Test
    fun `retry reloads settings`() = runTest {
        val vm = viewModel()
        vm.uiState.first { it !is DesignUiState.Loading }

        vm.retry()

        // перезагрузка уходит через flowOn(IO) — ждём вызов, а не мгновенное состояние
        verify(timeout = 5000, atLeast = 2) { getThemeModeUseCase() }
    }

    @Test
    fun `setAccentColor performs click haptic and stores accent`() = runTest {
        val vm = viewModel()

        vm.setAccentColor(AccentColor.PURPLE)

        verify(exactly = 1) { hapticsManager.perform(HapticType.CLICK) }
        coVerify(exactly = 1) { setAccentColorUseCase(AccentColor.PURPLE) }
    }

    @Test
    fun `onThemeSelected performs click haptic and stores theme`() = runTest {
        val vm = viewModel()

        vm.onThemeSelected(ThemeMode.LIGHT)

        verify(exactly = 1) { hapticsManager.perform(HapticType.CLICK) }
        coVerify(exactly = 1) { setThemeModeUseCase(ThemeMode.LIGHT) }
    }

    @Test
    fun `setCustomColor stores color and switches accent to CUSTOM`() = runTest {
        val vm = viewModel()

        vm.setCustomColor(Color(0xFF123456))

        coVerify(exactly = 1) { setCustomAccentColorUseCase(Color(0xFF123456)) }
        coVerify(exactly = 1) { setAccentColorUseCase(AccentColor.CUSTOM) }
        // клик хаптик срабатывает и в setCustomColor, и во вложенном setAccentColor
        verify(exactly = 2) { hapticsManager.perform(HapticType.CLICK) }
    }
}
