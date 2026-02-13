import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import soft.divan.financemanager.Conf
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.lib

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
