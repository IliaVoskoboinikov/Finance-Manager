package soft.divan.financemanager.feature.designapp.impl.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

class EnumMapperTest {

    @Test
    fun `toEnumOrNull resolves exact name`() {
        assertThat("DARK".toEnumOrNull<ThemeMode>()).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `toEnumOrNull ignores case`() {
        assertThat("light".toEnumOrNull<ThemeMode>()).isEqualTo(ThemeMode.LIGHT)
        assertThat("MiNt".toEnumOrNull<AccentColor>()).isEqualTo(AccentColor.MINT)
    }

    @Test
    fun `toEnumOrNull resolves every ThemeMode`() {
        ThemeMode.entries.forEach { mode ->
            assertThat(mode.name.toEnumOrNull<ThemeMode>()).isEqualTo(mode)
        }
    }

    @Test
    fun `toEnumOrNull returns null for unknown name`() {
        assertThat("NEON".toEnumOrNull<ThemeMode>()).isNull()
        assertThat("".toEnumOrNull<ThemeMode>()).isNull()
    }
}
