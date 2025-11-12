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

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}


gradlePlugin {
    plugins {
        register("androidBaseConventionPlugin") {
            id = "soft.divan.android.base"
            implementationClass = "plugins.AndroidBaseConventionPlugin"
        }

        register("androidAppConventionPlugin") {
            id = "soft.divan.android.app"
            implementationClass = "plugins.AndroidAppConventionPlugin"
        }

        register("coreConventionPlugin") {
            id = "soft.divan.core"
            implementationClass = "plugins.CoreConventionPlugin"
        }

        register("featureApiConventionPlugin") {
            id = "soft.divan.feature.api"
            implementationClass = "plugins.FeatureApiConventionPlugin"
        }

        register("featureImplConventionPlugin") {
            id = "soft.divan.feature.impl"
            implementationClass = "plugins.FeatureImplConventionPlugin"
        }

        register("jvmLibraryConventionPlugin") {
            id = "soft.divan.jvm.library"
            implementationClass = "plugins.JvmLibraryConventionPlugin"
        }

        register("hiltConventionPlugin") {
            id = "soft.divan.hilt"
            implementationClass = "plugins.HiltConventionPlugin"
        }

        register("detektConventionPlugin") {
            id = "soft.divan.detekt"
            implementationClass = "plugins.DetektConventionPlugin"
        }

        register("BuildTimeTrackerConventionPlugin") {
            id = "soft.divan.build.time.tracker"
            implementationClass = "plugins.BuildTimeTrackerConventionPlugin"
        }
    }
}
