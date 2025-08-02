plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins.register("checkConventionsPlugin") {
        id = "check-conventions-plugin"
        implementationClass = "plugins.conventions.CheckConventionsPlugin"
    }
    plugins {
        register("hiltPlugin") {
            id = "android-hilt"
            implementationClass = "plugins.HiltPlugin"
        }
    }
}


dependencies {
    implementation(libs.agp)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
