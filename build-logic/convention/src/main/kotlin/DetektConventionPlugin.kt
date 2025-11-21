import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.libs

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("detekt").get().get().pluginId)

            project.extensions.configure<DetektExtension> {
                toolVersion = libs.findVersion("detekt").get().requiredVersion
                config.from(file("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                parallel = true
                ignoreFailures = true
            }
        }
    }
}