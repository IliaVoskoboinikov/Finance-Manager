package plugins

import baseAndroidConfig
import com.android.build.gradle.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FeatureImplModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.extensions.getByName("libs") as LibrariesForLibs

        project.pluginManager.apply(libs.plugins.soft.divan.android.base.get().pluginId)
        project.pluginManager.apply(libs.plugins.android.library.get().pluginId)
        project.pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)
        project.pluginManager.apply(libs.plugins.soft.divan.hilt.get().pluginId)


        project.extensions.configure<LibraryExtension> {
            baseAndroidConfig(project)

            buildFeatures {
                compose = true
            }

            composeOptions {
                kotlinCompilerExtensionVersion = "1.5.1"
            }
        }

        project.dependencies {
            add("implementation", libs.androidx.core.ktx)
            add("implementation", libs.androidx.appcompat)
            add("implementation", platform(libs.androidx.compose.bom))
            add("implementation", libs.androidx.ui)
            add("implementation", libs.androidx.material3)
            add("implementation", libs.material)
            add("debugImplementation", libs.androidx.ui.tooling)
            add("implementation", libs.androidx.hilt.navigation.compose)
        }
    }
}