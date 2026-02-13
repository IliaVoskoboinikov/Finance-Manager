import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.configureBaseAndroid

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
}
