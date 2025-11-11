@file:Suppress("MatchingDeclarationName")

import com.android.build.gradle.BaseExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies

internal val Project.libs: LibrariesForLibs
    get() = (this as ExtensionAware).extensions.getByName("libs") as LibrariesForLibs

fun BaseExtension.configureBaseAndroid(project: Project) {
    namespace = generateNamespace(project)
    setCompileSdkVersion(Const.COMPILE_SKD)

    defaultConfig {
        minSdk = Const.MIN_SKD
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
        targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
    }
}

fun addDefaultComposeDependencies(project: Project) {
    val libs = project.libs
    project.dependencies {
        add(Conf.IMPLEMENTATION, libs.androidx.core.ktx)
        add(Conf.IMPLEMENTATION, libs.androidx.appcompat)
        add(Conf.IMPLEMENTATION, project.libs.androidx.compose.bom)
        add(Conf.IMPLEMENTATION, libs.androidx.ui)
        add(Conf.IMPLEMENTATION, libs.androidx.ui.tooling.preview)
        add(Conf.IMPLEMENTATION, libs.androidx.material3)
        add(Conf.IMPLEMENTATION, libs.material)
        add(Conf.IMPLEMENTATION, libs.androidx.hilt.navigation.compose)
    }
}

private fun generateNamespace(project: Project): String {
    val root = Const.NAMESPACE
    val path = project.path

    return path.split(":")
        .drop(1)
        .joinToString(separator = ".") { it.replace("-", "_") }
        .let { if (it.isEmpty()) root else "$root.$it" }
}
