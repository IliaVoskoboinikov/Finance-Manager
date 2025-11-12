package plugins

import Conf
import libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.addDefaultComposeDependencies() {
    dependencies {
        dependencies {
            add(Conf.IMPLEMENTATION, libs.androidx.core.ktx)
            add(Conf.IMPLEMENTATION, libs.androidx.appcompat)
            add(Conf.IMPLEMENTATION, libs.androidx.compose.bom)
            add(Conf.IMPLEMENTATION, libs.androidx.ui)
            add(Conf.IMPLEMENTATION, libs.androidx.ui.tooling.preview)
            add(Conf.IMPLEMENTATION, libs.androidx.material3)
            add(Conf.IMPLEMENTATION, libs.androidx.hilt.navigation.compose)
        }
    }
}
