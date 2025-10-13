plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {

        register("checkConventionsPlugin") {
            id = "check-conventions-plugin"
            implementationClass = "plugins.conventions.CheckConventionsPlugin"
        }

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


    }
}


dependencies {
    implementation(libs.agp)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
