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
        register("androidBasePlugin") {
            id = "soft.divan.android.base"
            implementationClass = "plugins.AndroidBasePlugin"
        }

        register("androidAppPlugin") {
            id = "soft.divan.android.app"
            implementationClass = "plugins.AndroidAppModulePlugin"
        }

        register("coreModulePlugin") {
            id = "soft.divan.core"
            implementationClass = "plugins.CoreModulePlugin"
        }

        register("featureApiModulePlugin") {
            id = "soft.divan.feature.api"
            implementationClass = "plugins.FeatureApiModulePlugin"
        }

        register("featureImplModulePlugin") {
            id = "soft.divan.feature.impl"
            implementationClass = "plugins.FeatureImplModulePlugin"
        }

        register("jvmModulePlugin") {
            id = "jvm-module"
            implementationClass = "plugins.JvmModulePlugin"
        }

        register("hiltPlugin") {
            id = "soft.divan.hilt"
            implementationClass = "plugins.HiltPlugin"
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
