import ModuleType.isApp
import ModuleType.isCore
import ModuleType.isFeature
import ModuleType.isFeatureApi
import ModuleType.isFeatureImpl
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class ModuleTypeTest {

    /* ---------- String predicates ---------- */

    @Test
    fun `isCore matches only core module paths`() {
        assertThat(":core:data".isCore()).isTrue()
        assertThat(":core:domain".isCore()).isTrue()
        assertThat(":feature:auth:impl".isCore()).isFalse()
        assertThat(":app".isCore()).isFalse()
    }

    @Test
    fun `isFeature matches any feature module path`() {
        assertThat(":feature:auth:api".isFeature()).isTrue()
        assertThat(":feature:auth:impl".isFeature()).isTrue()
        assertThat(":core:data".isFeature()).isFalse()
    }

    @Test
    fun `isFeatureApi requires feature prefix and api suffix`() {
        assertThat(":feature:auth:api".isFeatureApi()).isTrue()
        assertThat(":feature:auth:impl".isFeatureApi()).isFalse()
        assertThat(":core:data:api".isFeatureApi()).isFalse()
    }

    @Test
    fun `isFeatureImpl requires feature prefix and impl suffix`() {
        assertThat(":feature:auth:impl".isFeatureImpl()).isTrue()
        assertThat(":feature:auth:api".isFeatureImpl()).isFalse()
        assertThat(":core:data".isFeatureImpl()).isFalse()
    }

    /* ---------- Project extensions ---------- */

    @Test
    fun `project extensions classify by path`() {
        val graph = ProjectGraph()
        val core = graph.at(":core:data")
        val featureImpl = graph.at(":feature:auth:impl")
        val featureApi = graph.at(":feature:auth:api")
        val app = graph.at(":app")

        assertThat(core.isCore()).isTrue()
        assertThat(featureImpl.isFeatureImpl()).isTrue()
        assertThat(featureImpl.isFeature()).isTrue()
        assertThat(featureApi.isFeatureApi()).isTrue()
        assertThat(app.isApp()).isTrue()
        assertThat(core.isApp()).isFalse()
    }
}

/**
 * Строит дерево тестовых Gradle-проектов с общим корнем, кэшируя промежуточные узлы,
 * чтобы можно было получить проект по произвольному пути (`:feature:auth:impl`).
 */
class ProjectGraph {
    private val root = ProjectBuilder.builder().withName("root").build()
    private val cache = mutableMapOf(":" to root)

    fun at(path: String): org.gradle.api.Project = cache.getOrPut(path) {
        val parentPath = path.substringBeforeLast(":").ifEmpty { ":" }
        val parent = at(parentPath)
        val name = path.substringAfterLast(":")
        ProjectBuilder.builder().withName(name).withParent(parent).build()
    }
}
