package plugins

import Conf
import Const
import libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.libs
        project.pluginManager.apply(libs.plugins.java.library.get().pluginId)
        project.pluginManager.apply(libs.plugins.jetbrains.kotlin.jvm.get().pluginId)

        project.extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
            targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
        }

        project.extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(Const.JAVA_VERSION))
            }
        }

        project.dependencies {
            add(Conf.IMPLEMENTATION, libs.kotlinx.coroutines.core)
            add(Conf.IMPLEMENTATION, libs.javax.inject)
        }
    }
}