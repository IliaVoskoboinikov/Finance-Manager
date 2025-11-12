package plugins

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.plugins.detekt.get().pluginId)

            project.extensions.configure<DetektExtension> {
                toolVersion = libs.versions.detekt.get()
                config.from(file("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                parallel = true
                ignoreFailures = true
            }
        }
    }
}