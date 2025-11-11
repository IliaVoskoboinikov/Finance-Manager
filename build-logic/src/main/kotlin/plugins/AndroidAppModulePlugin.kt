package plugins

import addDefaultComposeDependencies
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import configureBaseAndroid
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val libs = project.libs
        project.pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
        project.pluginManager.apply(libs.plugins.android.application.get().pluginId)
        project.pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)

        project.extensions.configure<BaseAppModuleExtension> {
            configureBaseAndroid(project)
            buildFeatures.compose = true
        }

        addDefaultComposeDependencies(project)
    }
}
