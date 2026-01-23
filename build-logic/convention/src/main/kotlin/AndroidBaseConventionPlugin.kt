import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import soft.divan.finansemanager.applyPlugin

class AndroidBaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("kotlin-android")


            pluginManager.withPlugin("com.android.application") {
                addLintChecksDependency()
            }

            pluginManager.withPlugin("com.android.library") {
                addLintChecksDependency()
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(Const.JAVA_VERSION))
                }
            }
        }
    }

    private fun Project.addLintChecksDependency() {
        dependencies {
            add("lintChecks", project(":lint"))
        }
    }

}