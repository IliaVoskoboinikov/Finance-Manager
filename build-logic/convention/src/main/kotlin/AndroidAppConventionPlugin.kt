import com.android.build.gradle.internal.dsl.BaseAppModuleExtension

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.addDefaultComposeDependencies
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.configureBaseAndroid

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-application")
            applyPlugin("kotlin-compose")
            applyPlugin("soft-divan-hilt")
            applyPlugin("soft-divan-firebase")

            applyPlugin("graph")
            applyPlugin("soft-divan-detect")
            applyPlugin("soft-divan-build-time-tracker")

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
