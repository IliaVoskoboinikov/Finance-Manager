import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.junit.Test

class PluginChecksTest {

    private val checks = PluginChecks()

    private fun project(path: String, vararg plugins: String): Project {
        val pluginManager = mockk<PluginManager>()
        val applied = plugins.toSet()
        every { pluginManager.hasPlugin(any<String>()) } answers { firstArg<String>() in applied }
        val project = mockk<Project>()
        every { project.path } returns path
        every { project.pluginManager } returns pluginManager
        return project
    }

    /* ---------- core ---------- */

    @Test
    fun `core module without convention plugin is rejected`() {
        assertThatThrownBy { checks.checkCoreModule(project(":core:data")) }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("must apply one of")
    }

    @Test
    fun `core module with both android and jvm conventions is rejected`() {
        val project = project(":core:data", "soft.divan.core", "soft.divan.jvm.library")

        assertThatThrownBy { checks.checkCoreModule(project) }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("cannot apply both")
    }

    @Test
    fun `core module with a single convention plugin passes`() {
        assertThatCode {
            checks.checkCoreModule(project(":core:data", "soft.divan.core"))
        }.doesNotThrowAnyException()
        assertThatCode {
            checks.checkCoreModule(project(":core:domain", "soft.divan.jvm.library"))
        }.doesNotThrowAnyException()
    }

    /* ---------- feature ---------- */

    @Test
    fun `feature module with both api and impl plugins is rejected`() {
        val project = project(
            ":feature:auth:impl",
            "soft.divan.feature.api",
            "soft.divan.feature.impl"
        )

        assertThatThrownBy { checks.checkFeatureModule(project) }
            .hasMessageContaining("cannot apply both api and impl")
    }

    @Test
    fun `feature api plugin on a non-api module is rejected`() {
        val project = project(":feature:auth:impl", "soft.divan.feature.api")

        assertThatThrownBy { checks.checkFeatureModule(project) }
            .hasMessageContaining("Expected suffix :api")
    }

    @Test
    fun `feature impl plugin on a non-impl module is rejected`() {
        val project = project(":feature:auth:api", "soft.divan.feature.impl")

        assertThatThrownBy { checks.checkFeatureModule(project) }
            .hasMessageContaining("Expected suffix :impl")
    }

    @Test
    fun `feature plugins applied to matching modules pass`() {
        assertThatCode {
            checks.checkFeatureModule(project(":feature:auth:api", "soft.divan.feature.api"))
        }.doesNotThrowAnyException()
        assertThatCode {
            checks.checkFeatureModule(project(":feature:auth:impl", "soft.divan.feature.impl"))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `feature module without feature plugins passes`() {
        assertThatCode {
            checks.checkFeatureModule(project(":feature:auth:impl"))
        }.doesNotThrowAnyException()
    }

    /* ---------- app ---------- */

    @Test
    fun `app module without app plugin is rejected`() {
        assertThatThrownBy { checks.checkAppModule(project(":app")) }
            .hasMessageContaining("must apply soft.divan.android.app")
    }

    @Test
    fun `app module with app plugin passes`() {
        assertThatCode {
            checks.checkAppModule(project(":app", "soft.divan.android.app"))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `violation message includes the module path`() {
        assertThatThrownBy { checks.checkCoreModule(project(":core:secret")) }
            .hasMessageContaining(":core:secret")
    }
}
