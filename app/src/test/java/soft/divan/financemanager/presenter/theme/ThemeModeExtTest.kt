package soft.divan.financemanager.presenter.theme

import android.app.UiModeManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode

class ThemeModeExtTest {

    @Test
    fun `explicit modes ignore system theme`() {
        assertThat(ThemeMode.LIGHT.isDarkTheme(isSystemDark = true)).isFalse()
        assertThat(ThemeMode.DARK.isDarkTheme(isSystemDark = false)).isTrue()
    }

    @Test
    fun `system mode follows system theme`() {
        assertThat(ThemeMode.SYSTEM.isDarkTheme(isSystemDark = true)).isTrue()
        assertThat(ThemeMode.SYSTEM.isDarkTheme(isSystemDark = false)).isFalse()
    }

    @Test
    fun `maps to application night mode`() {
        assertThat(ThemeMode.LIGHT.toApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_NO)
        assertThat(ThemeMode.DARK.toApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_YES)
        assertThat(ThemeMode.SYSTEM.toApplicationNightMode())
            .isEqualTo(UiModeManager.MODE_NIGHT_AUTO)
    }
}
