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
                abi.set(ABI)
                locale.set(LOCALE)
                screenDensity.set(SCREEN_DENSITY)
                sdkVersion.set(SDK_VERSION)
            }
        }
    }

    companion object {
        private const val SCREEN_DENSITY = 480
        private const val SDK_VERSION = 27
        private const val ABI = "arm64-v8a"
        private const val LOCALE = "en"
    }
}
