import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Функциональные тесты через Gradle TestKit: реально запускают сборку временного проекта,
 * который применяет `soft.divan.check.conventions`, и проверяют, что нарушения конвенций
 * действительно валят сборку, а корректная структура проходит.
 */
class CheckConventionsFunctionalTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun rootBuildFile() {
        file(
            "build.gradle.kts",
            """
            plugins {
                id("soft.divan.check.conventions")
            }
            """.trimIndent()
        )
    }

    private fun file(path: String, content: String) {
        val target = File(tempFolder.root, path)
        target.parentFile.mkdirs()
        target.writeText(content)
    }

    private fun runner() = GradleRunner.create()
        .withProjectDir(tempFolder.root)
        .withPluginClasspath()
        .withArguments("help", "--stacktrace")

    @Test
    fun `build fails when a core module is missing its convention plugin`() {
        file("settings.gradle.kts", """rootProject.name = "fixture"; include(":core:sample")""")
        rootBuildFile()
        file("core/sample/build.gradle.kts", "// intentionally no convention plugin")

        val result = runner().buildAndFail()

        assertThat(result.output).contains("Core modules must apply one of")
        assertThat(result.output).contains(":core:sample")
    }

    @Test
    fun `build fails when a feature api module depends on a feature impl module`() {
        // Пустой feature-модуль проходит проверку плагинов (ни api, ни impl не применён),
        // поэтому изолированно срабатывает именно проверка архитектурной зависимости.
        file(
            "settings.gradle.kts",
            """rootProject.name = "fixture"; include(":feature:x:api", ":feature:y:impl")"""
        )
        rootBuildFile()
        file(
            "feature/x/api/build.gradle.kts",
            """
            configurations.create("api")
            dependencies { add("api", project(":feature:y:impl")) }
            """.trimIndent()
        )
        file("feature/y/impl/build.gradle.kts", "// bare feature impl")

        val result = runner().buildAndFail()

        assertThat(result.output)
            .contains("Feature api modules must NOT depend on feature impl modules")
    }

    @Test
    fun `build succeeds when module structure follows the conventions`() {
        // модуль :tools:sample не является core/feature/app, поэтому проверки плагинов его не касаются
        file("settings.gradle.kts", """rootProject.name = "fixture"; include(":tools:sample")""")
        rootBuildFile()
        file("tools/sample/build.gradle.kts", "// no architecture violations")

        val result = runner().build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }
}
