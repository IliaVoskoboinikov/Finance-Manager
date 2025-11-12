package plugins

import Conf
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.plugins.hilt.get().pluginId)
            pluginManager.apply(libs.plugins.ksp.get().pluginId)

            dependencies {
                add(Conf.IMPLEMENTATION, libs.hilt.android)
                add(Conf.KSP, libs.hilt.compiler)
            }
        }
    }
}
