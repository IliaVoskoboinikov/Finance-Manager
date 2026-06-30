package soft.divan.financemanager.uikit.theme

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AccentColorTest {

    @Test
    fun `custom and dynamic have no predefined color`() {
        assertThat(AccentColor.CUSTOM.color).isNull()
        assertThat(AccentColor.DYNAMIC.color).isNull()
    }

    @Test
    fun `predefined accents map to their palette colors`() {
        assertThat(AccentColor.MINT.color).isEqualTo(NeonMint)
        assertThat(AccentColor.PURPLE.color).isEqualTo(Purple)
        assertThat(AccentColor.ORANGE.color).isEqualTo(Orange)
        assertThat(AccentColor.BLUE.color).isEqualTo(Blue)
        assertThat(AccentColor.PINK.color).isEqualTo(Pink)
    }

    @Test
    fun `only custom and dynamic accents are colorless`() {
        val colorless = AccentColor.entries.filter { it.color == null }

        assertThat(colorless).containsExactlyInAnyOrder(AccentColor.CUSTOM, AccentColor.DYNAMIC)
    }

    @Test
    fun `exposes the expected set of accents`() {
        assertThat(AccentColor.entries).hasSize(7)
    }
}
