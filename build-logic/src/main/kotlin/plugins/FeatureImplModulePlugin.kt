package plugins

import Conf
import addDefaultComposeDependencies
import com.android.build.gradle.LibraryExtension
import configureBaseAndroid
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FeatureImplModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.libs
        project.pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
        project.pluginManager.apply(libs.plugins.android.library.get().pluginId)
        project.pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)
        project.pluginManager.apply(libs.plugins.soft.divan.hilt.get().pluginId)

        project.extensions.configure<LibraryExtension> {
            configureBaseAndroid(project)
            buildFeatures {
                compose = true
            }
        }

        addDefaultComposeDependencies(project)

        project.dependencies {
            add(Conf.DEBUG_IMPLEMENTATION, libs.androidx.ui.tooling)
        }
    }
}