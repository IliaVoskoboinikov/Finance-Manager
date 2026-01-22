package soft.divan.finansemanager

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.addDefaultComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add(Conf.IMPLEMENTATION, platform(bom))
        add(Conf.IMPLEMENTATION, lib("androidx-ui-tooling-preview"))
        add(Conf.IMPLEMENTATION, lib("androidx-material3"))
        add(Conf.IMPLEMENTATION, lib("hilt-navigation-compose"))
    }
}
