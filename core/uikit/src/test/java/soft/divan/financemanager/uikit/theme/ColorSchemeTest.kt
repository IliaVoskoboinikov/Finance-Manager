package soft.divan.financemanager.uikit.theme

import androidx.compose.ui.graphics.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ColorSchemeTest {

    private val primary = Color(0xFF123456)

    @Test
    fun `light scheme uses the provided primary for primary and surface`() {
        val scheme = baseLightColorScheme(primary)

        assertThat(scheme.primary).isEqualTo(primary)
        assertThat(scheme.surface).isEqualTo(primary)
    }

    @Test
    fun `light scheme maps brand colors`() {
        val scheme = baseLightColorScheme(primary)

        assertThat(scheme.background).isEqualTo(RoseWhite)
        assertThat(scheme.primaryContainer).isEqualTo(NeonMint)
        assertThat(scheme.onPrimaryContainer).isEqualTo(White)
        assertThat(scheme.surfaceContainer).isEqualTo(LavenderMist)
        assertThat(scheme.onSecondaryContainer).isEqualTo(NeonMint)
        assertThat(scheme.secondaryContainer).isEqualTo(MintBackground)
        assertThat(scheme.onSurfaceVariant).isEqualTo(Graphite)
        assertThat(scheme.onSurface).isEqualTo(CharcoalPurple)
        assertThat(scheme.error).isEqualTo(CoralRed)
        assertThat(scheme.surfaceContainerHigh).isEqualTo(SoftLavender)
    }

    @Test
    fun `dark scheme uses the provided primary for primary and surface`() {
        val scheme = baseDarkColorScheme(primary)

        assertThat(scheme.primary).isEqualTo(primary)
        assertThat(scheme.surface).isEqualTo(primary)
    }

    @Test
    fun `dark scheme maps brand colors`() {
        val scheme = baseDarkColorScheme(primary)

        assertThat(scheme.background).isEqualTo(BlackDark)
        assertThat(scheme.primaryContainer).isEqualTo(MintBackgroundDark)
        assertThat(scheme.onPrimaryContainer).isEqualTo(BlackDark)
        assertThat(scheme.surfaceContainer).isEqualTo(RoseWhiteDark)
        assertThat(scheme.onSecondaryContainer).isEqualTo(WhiteDark)
        assertThat(scheme.secondaryContainer).isEqualTo(SoftLavenderDark)
        assertThat(scheme.onSurfaceVariant).isEqualTo(GraphiteDark)
        assertThat(scheme.onSurface).isEqualTo(WhiteDark)
        assertThat(scheme.error).isEqualTo(CoralRedDark)
        assertThat(scheme.surfaceContainerHigh).isEqualTo(LavenderMistDark)
    }

    @Test
    fun `light and dark schemes differ for the same primary`() {
        assertThat(baseLightColorScheme(primary).background)
            .isNotEqualTo(baseDarkColorScheme(primary).background)
    }
}
