package plugins

import Const
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension


class JvmModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.extensions.getByName("libs") as LibrariesForLibs

        project.pluginManager.apply(libs.plugins.java.library.get().pluginId)
        project.pluginManager.apply(libs.plugins.jetbrains.kotlin.jvm.get().pluginId)

        project.extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            sourceCompatibility = Const.COMPILE_JDK_VERSION
            targetCompatibility = Const.COMPILE_JDK_VERSION
        }

        project.extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        project.dependencies {
            add("implementation", libs.kotlinx.coroutines.core)
            add("implementation", libs.javax.inject)
        }
    }
}