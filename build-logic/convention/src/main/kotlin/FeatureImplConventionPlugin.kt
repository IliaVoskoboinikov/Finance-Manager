import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import soft.divan.financemanager.Conf
import soft.divan.financemanager.addDefaultComposeDependencies
import soft.divan.financemanager.addDefaultUnitTestDependencies
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.configureBaseAndroid
import soft.divan.financemanager.configureNavGraph
import soft.divan.financemanager.lib

class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("soft-divan-android-base")
            applyPlugin("android-library")
            applyPlugin("kotlin-compose")
            applyPlugin("soft-divan-hilt")

            // Экраны фичи попадают в общий граф навигации (docs/nav-graph.md):
            // KSP-процессор разбирает аннотации и складывает граф фичи в nav-graph.json,
            // который агрегирует :app.
            configureNavGraph()

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

            addDefaultUnitTestDependencies()
        }
    }
}
