import com.spotify.ruler.plugin.RulerExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.financemanager.applyPlugin

class RulerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugin("ruler")
            project.extensions.configure<RulerExtension> {
                abi.set("arm64-v8a")
                locale.set("en")
                screenDensity.set(480)
                sdkVersion.set(27)
            }
        }
    }
}
