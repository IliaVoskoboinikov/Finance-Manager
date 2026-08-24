plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.agp)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.plugin)

    // В отличие от compose-плагина, плагин kotlinx.serialization не входит в KGP,
    // поэтому его нужно положить на runtime-classpath convention-плагинов —
    // иначе pluginManager.apply("org.jetbrains.kotlin.plugin.serialization") его не найдёт.
    implementation(libs.kotlin.serialization.plugin)
    compileOnly(libs.build.time.tracker)
    compileOnly(libs.ruler.plugin)
    compileOnly(libs.dependency.guard.plugin)

    // Нужен только ради типов NavGraphExtension / RenderBackend в convention-плагинах:
    // сам плагин navgraph подключается через root build.gradle.kts (`apply false`).
    compileOnly(libs.navgraph.gradle.plugin)

    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        register("androidBaseConventionPlugin") {
            id = libs.plugins.soft.divan.android.base.get().pluginId
            implementationClass = "AndroidBaseConventionPlugin"
        }

        register("androidAppConventionPlugin") {
            id = libs.plugins.soft.divan.android.app.get().pluginId
            implementationClass = "AndroidAppConventionPlugin"
        }

        register("coreConventionPlugin") {
            id = libs.plugins.soft.divan.core.get().pluginId
            implementationClass = "CoreConventionPlugin"
        }

        register("featureApiConventionPlugin") {
            id = libs.plugins.soft.divan.feature.api.get().pluginId
            implementationClass = "FeatureApiConventionPlugin"
        }

        register("featureImplConventionPlugin") {
            id = libs.plugins.soft.divan.feature.impl.get().pluginId
            implementationClass = "FeatureImplConventionPlugin"
        }

        register("jvmLibraryConventionPlugin") {
            id = libs.plugins.soft.divan.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }

        register("hiltConventionPlugin") {
            id = libs.plugins.soft.divan.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }

        register("buildTimeTrackerConventionPlugin") {
            id = libs.plugins.soft.divan.build.time.tracker.get().pluginId
            implementationClass = "BuildTimeTrackerConventionPlugin"
        }

        register("androidAppFirebaseConventionPlugin") {
            id = libs.plugins.soft.divan.firebase.get().pluginId
            implementationClass = "AndroidAppFirebaseConventionPlugin"
        }

        plugins.register("checkConventionsPlugin") {
            id = libs.plugins.soft.divan.check.conventions.get().pluginId
            implementationClass = "CheckConventionsPlugin"
        }

        plugins.register("rulerConventionPlugin") {
            id = libs.plugins.soft.divan.ruler.get().pluginId
            implementationClass = "RulerConventionPlugin"
        }

        plugins.register("dependencyGuardConventionPlugin") {
            id = libs.plugins.soft.divan.dependency.guard.get().pluginId
            implementationClass = "DependencyGuardConventionPlugin"
        }
    }
}
