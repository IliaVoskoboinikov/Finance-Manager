package soft.divan.finansemanager

import Conf
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.addDefaultComposeDependencies() {
    dependencies {
        add(Conf.IMPLEMENTATION, libs.findLibrary("androidx-core-ktx").get())
        add(Conf.IMPLEMENTATION, libs.findLibrary("androidx-appcompat").get())
        add(Conf.IMPLEMENTATION, libs.findLibrary("androidx-compose-bom").get())
        add(Conf.IMPLEMENTATION, libs.findLibrary("androidx-ui-tooling-preview").get())
        add(Conf.IMPLEMENTATION, libs.findLibrary("androidx-material3").get())
        add(Conf.IMPLEMENTATION, libs.findLibrary("hilt-navigation-compose").get())
    }
}
