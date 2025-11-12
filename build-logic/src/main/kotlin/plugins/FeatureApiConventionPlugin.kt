package plugins

import com.android.build.gradle.LibraryExtension
import configureBaseAndroid
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class FeatureApiConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
            pluginManager.apply(libs.plugins.android.library.get().pluginId)

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
            }
        }
    }
}