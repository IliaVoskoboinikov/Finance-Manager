import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.configureBaseAndroid

class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-library")

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
            }
        }
    }
}