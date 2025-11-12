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
        with(project) {
            pluginManager.apply(libs.plugins.java.library.get().pluginId)
            pluginManager.apply(libs.plugins.jetbrains.kotlin.jvm.get().pluginId)

            extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
                sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
                targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(Const.JAVA_VERSION))
                }
            }

            dependencies {
                add(Conf.IMPLEMENTATION, libs.kotlinx.coroutines.core)
                add(Conf.IMPLEMENTATION, libs.javax.inject)
            }
        }
    }
}