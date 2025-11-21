import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.configureBaseAndroid
import soft.divan.finansemanager.libs

class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("soft-divan-android-base").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("android-library").get().get().pluginId)

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
            }
        }
    }
}