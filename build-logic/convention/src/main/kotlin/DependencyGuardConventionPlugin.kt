import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.financemanager.applyPlugin

/**
 * Фиксирует дерево зависимостей приложения слепком
 * `app/dependencies/releaseRuntimeClasspath.txt`.
 *
 * Любое изменение release-classpath — в том числе транзитивное, приехавшее с чужим
 * обновлением, — становится видимым diff'ом в PR, а не сюрпризом в релизе.
 *
 * Обновить слепок осознанно: `./gradlew :app:dependencyGuardBaseline`,
 * проверить — `./gradlew :app:dependencyGuard`.
 *
 * Проверяется именно `releaseRuntimeClasspath`: это то, что реально уезжает
 * пользователю. Debug-варианты и тестовые конфигурации меняются часто и
 * на состав APK не влияют.
 */
class DependencyGuardConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugin("dependency-guard")
            extensions.configure<DependencyGuardPluginExtension> {
                configuration(RELEASE_RUNTIME_CLASSPATH)
            }
        }
    }

    companion object {
        private const val RELEASE_RUNTIME_CLASSPATH = "releaseRuntimeClasspath"
    }
}
