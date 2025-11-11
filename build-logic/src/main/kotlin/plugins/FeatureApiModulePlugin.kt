package plugins

import com.android.build.gradle.LibraryExtension
import configureBaseAndroid
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class FeatureApiModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.libs

        project.pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
        project.pluginManager.apply(libs.plugins.android.library.get().pluginId)

        project.extensions.configure<LibraryExtension> {
            configureBaseAndroid(project)
        }
    }
}