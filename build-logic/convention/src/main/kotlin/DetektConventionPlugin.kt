import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.version

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("detekt")
            project.extensions.configure<DetektExtension> {
                toolVersion = version("detekt")
                config.from(rootProject.file("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                parallel = true
                ignoreFailures = true
            }

            tasks.withType<Detekt>().configureEach {
                exclude("**/icons/**")
                exclude("**/Color.kt")
            }
        }
    }
}