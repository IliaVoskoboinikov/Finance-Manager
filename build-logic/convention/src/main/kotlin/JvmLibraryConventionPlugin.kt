import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import soft.divan.finansemanager.Conf
import soft.divan.finansemanager.applyPlugin
import soft.divan.finansemanager.lib

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("java-library")
            applyPlugin("jetbrains-kotlin-jvm")

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
                targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(Const.JAVA_VERSION))
                }
            }

            dependencies {
                add(Conf.IMPLEMENTATION, lib("kotlinx-coroutines-core"))
                add(Conf.IMPLEMENTATION, lib("javax-inject"))
            }
        }
    }
}