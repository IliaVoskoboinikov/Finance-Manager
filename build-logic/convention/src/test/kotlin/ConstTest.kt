import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConstTest {

    @Test
    fun `namespace and sdk constants match project configuration`() {
        assertThat(Const.NAMESPACE).isEqualTo("soft.divan.financemanager")
        assertThat(Const.COMPILE_SKD).isEqualTo(36)
        assertThat(Const.MIN_SKD).isEqualTo(26)
        assertThat(Const.JAVA_VERSION).isEqualTo("11")
    }

    @Test
    fun `version constants are present`() {
        assertThat(Const.VERSION_CODE).isEqualTo(1)
        assertThat(Const.VERSION_NAME).isEqualTo("0.0.1")
    }

    @Test
    fun `min sdk is not greater than compile sdk`() {
        assertThat(Const.MIN_SKD).isLessThanOrEqualTo(Const.COMPILE_SKD)
    }
}
