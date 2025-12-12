plugins {
    `kotlin-dsl`
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
    compileOnly(libs.detekt.gradle)
    compileOnly(libs.build.time.tracker)
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

        register("detektConventionPlugin") {
            id = libs.plugins.soft.divan.detect.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }

        register("buildTimeTrackerConventionPlugin") {
            id = libs.plugins.soft.divan.build.time.tracker.get().pluginId
            implementationClass = "BuildTimeTrackerConventionPlugin"
        }

        register("androidAppFirebaseConventionPlugin") {
            id = libs.plugins.soft.divan.firebase.get().pluginId
            implementationClass = "AndroidAppFirebaseConventionPlugin"
        }
    }
}
