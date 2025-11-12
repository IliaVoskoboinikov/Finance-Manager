package plugins

import Const
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import configureBaseAndroid
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
            pluginManager.apply(libs.plugins.android.application.get().pluginId)
            pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)
            pluginManager.apply(libs.plugins.soft.divan.hilt.get().pluginId)

            pluginManager.apply(libs.plugins.graph.get().pluginId)
            pluginManager.apply(libs.plugins.soft.divan.detect.get().pluginId)
            pluginManager.apply(libs.plugins.soft.divan.buld.time.tracker.get().pluginId)

            extensions.configure<BaseAppModuleExtension> {
                configureBaseAndroid(project)

                defaultConfig {
                    versionCode = Const.VERSION_CODE
                    versionName = Const.VERSION_NAME
                    targetSdk = Const.COMPILE_SKD
                }

                buildFeatures.compose = true
            }

            addDefaultComposeDependencies()
        }
    }
}
