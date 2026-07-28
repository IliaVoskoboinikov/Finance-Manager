package soft.divan.financemanager.uikit.theme

import androidx.compose.ui.graphics.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ColorsTest {

    @Test
    fun `light palette exposes expected argb values`() {
        assertThat(Purple80).isEqualTo(Color(0xFFF50000))
        assertThat(NeonMint).isEqualTo(Color(0xFF2AE881))
        assertThat(Purple).isEqualTo(Color(0xFF9C27B0))
        assertThat(Orange).isEqualTo(Color(0xFFFF9800))
        assertThat(Blue).isEqualTo(Color(0xFF2196F3))
        assertThat(Pink).isEqualTo(Color(0xFFE91E63))
        assertThat(MintBackground).isEqualTo(Color(0xFFD4FAE6))
        assertThat(Black).isEqualTo(Color(0xFF000000))
        assertThat(CharcoalPurple).isEqualTo(Color(0xFF1D1B20))
        assertThat(White).isEqualTo(Color(0xFFFFFFFF))
        assertThat(Graphite).isEqualTo(Color(0xFF49454F))
        assertThat(LavenderMist).isEqualTo(Color(0xFFF3EDF7))
        assertThat(RoseWhite).isEqualTo(Color(0xFFFEF7FF))
        assertThat(CoralRed).isEqualTo(Color(0xFFE46962))
        assertThat(SoftLavender).isEqualTo(Color(0xFFF7F2FA))
    }

    @Test
    fun `dark palette exposes expected argb values`() {
        assertThat(Purple80Dark).isEqualTo(Color(0xFFCF6679))
        assertThat(NeonMintDark).isEqualTo(Color(0xFF00C57A))
        assertThat(MintBackgroundDark).isEqualTo(Color(0xFF1A3C2D))
        assertThat(BlackDark).isEqualTo(Color(0xFF121212))
        assertThat(CharcoalPurpleDark).isEqualTo(Color(0xFFE6E1E5))
        assertThat(WhiteDark).isEqualTo(Color(0xFFFFFFFF))
        assertThat(GraphiteDark).isEqualTo(Color(0xFFCAC4D0))
        assertThat(LavenderMistDark).isEqualTo(Color(0xFF2A2A2E))
        assertThat(RoseWhiteDark).isEqualTo(Color(0xFF2D1B2F))
        assertThat(CoralRedDark).isEqualTo(Color(0xFFB3261E))
        assertThat(SoftLavenderDark).isEqualTo(Color(0xFF3E2C46))
    }

    @Test
    fun `all palette colors are fully opaque`() {
        val palette = listOf(
            Purple80, NeonMint, Purple, Orange, Blue, Pink, MintBackground, Black,
            CharcoalPurple, White, Graphite, LavenderMist, RoseWhite, CoralRed, SoftLavender,
            Purple80Dark, NeonMintDark, MintBackgroundDark, BlackDark, CharcoalPurpleDark,
            WhiteDark, GraphiteDark, LavenderMistDark, RoseWhiteDark, CoralRedDark,
            SoftLavenderDark
        )

        palette.forEach { color ->
            assertThat(color.alpha).isEqualTo(1f)
        }
    }
}
