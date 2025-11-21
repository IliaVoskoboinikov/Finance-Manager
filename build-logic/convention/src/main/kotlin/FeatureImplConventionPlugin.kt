import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import soft.divan.finansemanager.addDefaultComposeDependencies
import soft.divan.finansemanager.configureBaseAndroid
import soft.divan.finansemanager.libs

class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.findPlugin("soft-divan-android-base").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("android-library").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("soft-divan-hilt").get().get().pluginId)

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
                buildFeatures {
                    compose = true
                }
            }

            addDefaultComposeDependencies()

            dependencies {
                add(Conf.DEBUG_IMPLEMENTATION, libs.findLibrary("androidx-ui-tooling").get())
            }
        }
    }
}