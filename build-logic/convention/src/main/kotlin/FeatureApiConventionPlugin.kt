import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import soft.divan.financemanager.Conf
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.configureBaseAndroid
import soft.divan.financemanager.lib

class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-library")

            // Ключи навигации (NavKey) объявляются в :api и сохраняются Navigation 3
            // через kotlinx.serialization, поэтому компилятор-плагин нужен каждому :api модулю.
            applyPlugin("kotlin-serialization")

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
            }

            dependencies {
                add(Conf.API, lib("kotlinx-serialization-core"))
            }
        }
    }
}
