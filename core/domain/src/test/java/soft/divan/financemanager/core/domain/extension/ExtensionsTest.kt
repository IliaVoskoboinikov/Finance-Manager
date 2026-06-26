package soft.divan.financemanager.core.domain.extension

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `pretty drops decimals for whole float`() {
        assertThat(100f.pretty()).isEqualTo("100")
    }

    @Test
    fun `pretty drops decimals for zero`() {
        assertThat(0f.pretty()).isEqualTo("0")
    }

    @Test
    fun `pretty keeps decimals for fractional float`() {
        assertThat(100.5f.pretty()).isEqualTo("100.5")
    }

    @Test
    fun `pretty handles negative whole float`() {
        assertThat((-42f).pretty()).isEqualTo("-42")
    }

    @Test
    fun `pretty handles negative fractional float`() {
        assertThat((-42.25f).pretty()).isEqualTo("-42.25")
    }
}
