package soft.divan.financemanager.uikit.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TypeTest {

    @Test
    fun `bodyLarge is configured with default font family`() {
        val style = Typography.bodyLarge

        assertThat(style.fontFamily).isEqualTo(FontFamily.Default)
        assertThat(style.fontWeight).isEqualTo(FontWeight.Normal)
    }

    @Test
    fun `bodyLarge uses 16sp font with 24sp line height`() {
        val style = Typography.bodyLarge

        assertThat(style.fontSize).isEqualTo(16.sp)
        assertThat(style.lineHeight).isEqualTo(24.sp)
        assertThat(style.letterSpacing).isEqualTo(0.5.sp)
    }
}
