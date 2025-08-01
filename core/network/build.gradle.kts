import java.util.Properties

plugins {
    id("android-core-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val apiToken: String = project.rootProject
    .file("local.properties")
    .inputStream()
    .use { Properties().apply { load(it) } }
    .getProperty("api.token") ?: ""

android {
    namespace = "soft.divan.financemanager.network"
    defaultConfig {
        buildConfigField("String", "API_TOKEN", "\"$apiToken\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "HOST", "\"https://shmr-finance.ru/api/\"")
        }

        debug {
            buildConfigField("String", "HOST", "\"https://shmr-finance.ru/api/\"")
        }
    }
}

dependencies {
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.androidx.tracing.ktx)
}