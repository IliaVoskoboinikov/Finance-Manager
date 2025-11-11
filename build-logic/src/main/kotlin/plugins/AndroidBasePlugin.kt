package plugins

import Const
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.libs
        project.pluginManager.apply(libs.plugins.kotlin.android.get().pluginId)

        project.extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(Const.JAVA_VERSION))
            }
        }
    }
}