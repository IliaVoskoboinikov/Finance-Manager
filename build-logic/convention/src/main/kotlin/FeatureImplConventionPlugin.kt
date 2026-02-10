import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import soft.divan.finansemanager.Conf
import soft.divan.finansemanager.addDefaultComposeDependencies
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.configureBaseAndroid
import soft.divan.finansemanager.lib

class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-library")
            applyPlugin("kotlin-compose")
            applyPlugin("soft-divan-hilt")

            extensions.configure<LibraryExtension> {
                configureBaseAndroid(project)
                buildFeatures {
                    compose = true
                }
            }

            addDefaultComposeDependencies()

            dependencies {
                add(Conf.DEBUG_IMPLEMENTATION, lib("androidx-ui-tooling"))
            }
        }
    }
}// Revue me>>
