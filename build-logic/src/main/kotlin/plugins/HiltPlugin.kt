package plugins

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val libs = project.extensions.getByName("libs") as LibrariesForLibs

        project.pluginManager.apply(libs.plugins.hilt.get().pluginId)
        project.pluginManager.apply(libs.plugins.ksp.get().pluginId)

        project.dependencies {
            add("implementation", libs.hilt.android)
            add("ksp", libs.hilt.compiler)
        }
    }
}
