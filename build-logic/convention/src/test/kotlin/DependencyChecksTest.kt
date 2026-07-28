import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.junit.Test

class DependencyChecksTest {

    private val checks = DependencyChecks()
    private val graph = ProjectGraph()

    private fun dependency(fromPath: String, toPath: String, configuration: String): Project {
        val from = graph.at(fromPath)
        val to = graph.at(toPath)
        from.configurations.create(configuration)
        from.dependencies.add(configuration, to)
        return from
    }

    @Test
    fun `feature impl cannot depend on another feature impl`() {
        val from = dependency(":feature:auth:impl", ":feature:account:impl", "implementation")

        assertThatThrownBy { checks.validateArchitecture(from) }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("feature impl modules")
    }

    @Test
    fun `feature api cannot depend on feature impl`() {
        val from = dependency(":feature:auth:api", ":feature:account:impl", "api")

        assertThatThrownBy { checks.validateArchitecture(from) }
            .hasMessageContaining("Feature api modules must NOT depend on feature impl")
    }

    @Test
    fun `core cannot depend on a feature module`() {
        val from = dependency(":core:data", ":feature:account:api", "implementation")

        assertThatThrownBy { checks.validateArchitecture(from) }
            .hasMessageContaining("Core modules must NOT depend on feature modules")
    }

    @Test
    fun `feature impl may depend on core and feature api`() {
        val fromCore = dependency(":feature:history:impl", ":core:domain", "implementation")
        assertThatCode { checks.validateArchitecture(fromCore) }.doesNotThrowAnyException()

        val fromApi = dependency(":feature:history:impl", ":feature:account:api", "api")
        assertThatCode { checks.validateArchitecture(fromApi) }.doesNotThrowAnyException()
    }

    @Test
    fun `app may depend on feature impl`() {
        val from = dependency(":app", ":feature:account:impl", "implementation")

        assertThatCode { checks.validateArchitecture(from) }.doesNotThrowAnyException()
    }

    @Test
    fun `dependencies in non-checked configurations are ignored`() {
        // ktlintRuleset — не api/implementation, поэтому нарушение не проверяется
        val from = dependency(":feature:auth:impl", ":feature:account:impl", "ktlintRuleset")

        assertThatCode { checks.validateArchitecture(from) }.doesNotThrowAnyException()
    }

    @Test
    fun `module without project dependencies passes`() {
        val from = graph.at(":core:data")
        from.configurations.create("implementation")

        assertThatCode { checks.validateArchitecture(from) }.doesNotThrowAnyException()
    }
}
