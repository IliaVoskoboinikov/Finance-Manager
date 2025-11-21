package soft.divan.finansemanager

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.addDefaultComposeDependencies() {
    dependencies {
        add(Conf.IMPLEMENTATION, lib("androidx-core-ktx"))
        add(Conf.IMPLEMENTATION, lib("androidx-appcompat"))
        add(Conf.IMPLEMENTATION, lib("androidx-compose-bom"))
        add(Conf.IMPLEMENTATION, lib("androidx-ui-tooling-preview"))
        add(Conf.IMPLEMENTATION, lib("androidx-material3"))
        add(Conf.IMPLEMENTATION, lib("hilt-navigation-compose"))
    }
}
