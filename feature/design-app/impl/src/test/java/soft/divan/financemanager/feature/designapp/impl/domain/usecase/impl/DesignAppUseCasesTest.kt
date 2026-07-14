package soft.divan.financemanager.feature.designapp.impl.domain.usecase.impl

import androidx.compose.ui.graphics.Color
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.uikit.theme.AccentColor

class DesignAppUseCasesTest {

    private val repository = mockk<DesignAppRepository>(relaxUnitFun = true)

    @Test
    fun `GetThemeModeUseCase delegates to repository`() = runTest {
        every { repository.getThemeMode() } returns flowOf(ThemeMode.DARK)

        assertThat(GetThemeModeUseCaseImpl(repository)().first()).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `SetThemeModeUseCase delegates to repository`() = runTest {
        SetThemeModeUseCaseImpl(repository)(ThemeMode.LIGHT)

        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `GetAccentColorUseCase delegates to repository`() = runTest {
        every { repository.getAccentColor() } returns flowOf(AccentColor.ORANGE)

        assertThat(GetAccentColorUseCaseImpl(repository)().first()).isEqualTo(AccentColor.ORANGE)
    }

    @Test
    fun `SetAccentColorUseCase delegates to repository`() = runTest {
        SetAccentColorUseCaseImpl(repository)(AccentColor.PINK)

        coVerify(exactly = 1) { repository.setAccentColor(AccentColor.PINK) }
    }

    @Test
    fun `GetCustomAccentColorUseCase delegates to repository`() = runTest {
        every { repository.getCustomAccentColor() } returns flowOf(Color(0xFF123456))

        assertThat(GetCustomAccentColorUseCaseImpl(repository)().first())
            .isEqualTo(Color(0xFF123456))
    }

    @Test
    fun `SetCustomAccentColorUseCase delegates to repository`() = runTest {
        SetCustomAccentColorUseCaseImpl(repository)(Color(0xFF123456))

        coVerify(exactly = 1) { repository.setCustomAccentColor(Color(0xFF123456)) }
    }
}
