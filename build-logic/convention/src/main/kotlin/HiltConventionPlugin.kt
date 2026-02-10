import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import soft.divan.finansemanager.Conf
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.lib

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("hilt")
            applyPlugin("ksp")

            dependencies {
                add(Conf.IMPLEMENTATION, lib("hilt-android"))
                add(Conf.KSP, lib("hilt-compiler"))
            }
        }
    }
}
// Revue me>>
