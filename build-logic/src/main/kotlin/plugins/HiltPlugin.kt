package plugins

import Conf
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val libs = project.libs
        project.pluginManager.apply(libs.plugins.hilt.get().pluginId)
        project.pluginManager.apply(libs.plugins.ksp.get().pluginId)

        project.dependencies {
            add(Conf.IMPLEMENTATION, libs.hilt.android)
            add(Conf.KSP, libs.hilt.compiler)
        }
    }
}
