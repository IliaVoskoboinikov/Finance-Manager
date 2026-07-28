package soft.divan.financemanager.feature.designapp.impl.data.mapper

import androidx.compose.ui.graphics.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ColorMapperTest {

    @Test
    fun `toHexString formats opaque color as ARGB hex`() {
        assertThat(Color(0xFF123456).toHexString()).isEqualTo("#FF123456")
    }

    @Test
    fun `toHexString keeps alpha channel`() {
        assertThat(Color(0x80FF0000).toHexString()).isEqualTo("#80FF0000")
    }

    @Test
    fun `toHexString pads single-digit channels with zero`() {
        assertThat(Color(0xFF010203).toHexString()).isEqualTo("#FF010203")
    }

    @Test
    fun `toHexString handles pure black and white`() {
        assertThat(Color(0xFF000000).toHexString()).isEqualTo("#FF000000")
        assertThat(Color(0xFFFFFFFF).toHexString()).isEqualTo("#FFFFFFFF")
    }

    @Test
    fun `toHexString output is uppercase`() {
        val hex = Color(0xFFABCDEF).toHexString()

        assertThat(hex).isEqualTo(hex.uppercase())
        assertThat(hex).isEqualTo("#FFABCDEF")
    }
}
