import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.configureBaseAndroid

class CoreConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("android-library")
            applyPlugin("soft-divan-android-base")

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
            }
        }
    }
}// Revue me>>
