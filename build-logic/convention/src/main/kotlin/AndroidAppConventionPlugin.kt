import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.financemanager.addDefaultComposeDependencies
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.configureBaseAndroid

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-application")
            applyPlugin("kotlin-compose")
            applyPlugin("soft-divan-hilt")
            applyPlugin("soft-divan-firebase")

            applyPlugin("graph")
            applyPlugin("soft-divan-build-time-tracker")
            applyPlugin("soft-divan-check-conventions")
            applyPlugin("soft-divan-ruler")

            extensions.configure<BaseAppModuleExtension> {
                configureBaseAndroid(project)

                val versionCodeProp = project.findProperty("versionCode")?.toString()?.toInt()
                val versionNameProp = project.findProperty("versionName")?.toString()

                defaultConfig {
                    versionCode = versionCodeProp ?: Const.VERSION_CODE
                    versionName = versionNameProp ?: Const.VERSION_NAME
                    targetSdk = Const.COMPILE_SKD
                }

                buildFeatures.compose = true
            }

            addDefaultComposeDependencies()
        }
    }
}
