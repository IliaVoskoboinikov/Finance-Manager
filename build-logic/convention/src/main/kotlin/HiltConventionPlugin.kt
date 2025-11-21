import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import soft.divan.finansemanager.libs

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("hilt").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("ksp").get().get().pluginId)

            dependencies {
                add(Conf.IMPLEMENTATION, libs.findLibrary("hilt-android").get())
                add(Conf.KSP, libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
