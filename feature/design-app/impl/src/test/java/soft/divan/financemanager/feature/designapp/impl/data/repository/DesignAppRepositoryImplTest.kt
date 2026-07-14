package soft.divan.financemanager.feature.designapp.impl.data.repository

import androidx.compose.ui.graphics.Color
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

class DesignAppRepositoryImplTest {

    private val localSource = mockk<DesignAppLocalSource>(relaxUnitFun = true)
    private val repository = DesignAppRepositoryImpl(localSource)

    @Test
    fun `getThemeMode delegates to local source`() = runTest {
        every { localSource.getThemeMode() } returns flowOf(ThemeMode.DARK)

        assertThat(repository.getThemeMode().first()).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `setThemeMode delegates to local source`() = runTest {
        repository.setThemeMode(ThemeMode.LIGHT)

        coVerify(exactly = 1) { localSource.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `getAccentColor delegates to local source`() = runTest {
        every { localSource.getAccentColor() } returns flowOf(AccentColor.PURPLE)

        assertThat(repository.getAccentColor().first()).isEqualTo(AccentColor.PURPLE)
    }

    @Test
    fun `setAccentColor delegates to local source`() = runTest {
        repository.setAccentColor(AccentColor.BLUE)

        coVerify(exactly = 1) { localSource.setAccentColor(AccentColor.BLUE) }
    }

    @Test
    fun `getCustomAccentColor delegates to local source`() = runTest {
        every { localSource.getCustomAccentColor() } returns flowOf(Color(0xFF123456))

        assertThat(repository.getCustomAccentColor().first()).isEqualTo(Color(0xFF123456))
    }

    @Test
    fun `setCustomAccentColor delegates to local source`() = runTest {
        repository.setCustomAccentColor(Color(0xFF123456))

        coVerify(exactly = 1) { localSource.setCustomAccentColor(Color(0xFF123456)) }
    }
}
