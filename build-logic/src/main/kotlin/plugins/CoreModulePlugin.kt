package plugins

import baseAndroidConfig
import com.android.build.gradle.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure


class CoreModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.extensions.getByName("libs") as LibrariesForLibs

        project.pluginManager.apply(libs.plugins.android.library.get().pluginId)
        project.pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)

        project.extensions.configure<LibraryExtension> {
            baseAndroidConfig(project)
        }
    }
}