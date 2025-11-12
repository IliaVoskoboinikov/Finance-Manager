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
        register("hiltPlugin") {
            id = "android-hilt"
            implementationClass = "plugins.HiltPlugin"
        }

        register("androidBasePlugin") {
            id = "android-base"
            implementationClass = "plugins.AndroidBasePlugin"
        }

        register("androidAppPlugin") {
            id = "android-app-module"
            implementationClass = "plugins.AndroidAppModulePlugin"
        }

        register("coreModulePlugin") {
            id = "core-module"
            implementationClass = "plugins.CoreModulePlugin"
        }

        register("featureApiModulePlugin") {
            id = "featureApi-module"
            implementationClass = "plugins.FeatureApiModulePlugin"
        }

        register("featureImplModulePlugin") {
            id = "featureImpl-module"
            implementationClass = "plugins.FeatureImplModulePlugin"
        }

        register("jvmModulePlugin") {
            id = "jvm-module"
            implementationClass = "plugins.JvmModulePlugin"
        }

        register("DetektConventionPlugin") {
            id = "detekt-plugin"
            implementationClass = "plugins.DetektConventionPlugin"
        }

        register("BuildTimeTrackerConventionPlugin") {
            id = "build-time-tracker-plugin"
            implementationClass = "plugins.BuildTimeTrackerConventionPlugin"
        }
    }
}
