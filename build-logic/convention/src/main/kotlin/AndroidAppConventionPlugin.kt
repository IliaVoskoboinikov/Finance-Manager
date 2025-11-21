import com.android.build.gradle.internal.dsl.BaseAppModuleExtension

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.addDefaultComposeDependencies
import soft.divan.finansemanager.configureBaseAndroid
import soft.divan.finansemanager.libs

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("soft-divan-android-base").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("android-application").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("soft-divan-hilt").get().get().pluginId)

            pluginManager.apply(libs.findPlugin("graph").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("soft-divan-detect").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("soft-divan-build-time-tracker").get().get().pluginId)

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
