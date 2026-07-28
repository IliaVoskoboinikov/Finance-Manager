package soft.divan.financemanager

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfAndBuildTypeTest {

    @Test
    fun `configuration names match gradle conventions`() {
        assertThat(Conf.IMPLEMENTATION).isEqualTo("implementation")
        assertThat(Conf.DEBUG_IMPLEMENTATION).isEqualTo("debugImplementation")
        assertThat(Conf.TEST_IMPLEMENTATION).isEqualTo("testImplementation")
        assertThat(Conf.KSP).isEqualTo("ksp")
        assertThat(Conf.API).isEqualTo("api")
    }

    @Test
    fun `debug build type carries a suffixed application id`() {
        assertThat(FmBuildType.DEBUG.applicationIdSuffix).isEqualTo(".debug")
    }

    @Test
    fun `release build type has no application id suffix`() {
        assertThat(FmBuildType.RELEASE.applicationIdSuffix).isNull()
    }

    @Test
    fun `build type enum exposes debug and release`() {
        assertThat(FmBuildType.entries).containsExactly(FmBuildType.DEBUG, FmBuildType.RELEASE)
    }
}
