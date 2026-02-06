package soft.divan.finansemanager

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.applyPlugin(alias: String) = pluginManager.apply(libs.findPlugin(alias).get().get().pluginId)


fun Project.lib(alias: String): Any = libs.findLibrary(alias).get()

fun Project.version(alias: String): String = libs.findVersion(alias).get().requiredVersion


// Revue me>>
