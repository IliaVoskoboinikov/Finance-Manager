import java.util.Properties

plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

val apiToken: String = project.rootProject
    .file("local.properties")
    .inputStream()
    .use { Properties().apply { load(it) } }
    .getProperty("api.token") ?: ""

android {
    defaultConfig {
        buildConfigField("String", "API_TOKEN", "\"$apiToken\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            buildConfigField("String", "HOST", "\"https://shmr-finance.ru/api/\"")
        }

        debug {
            buildConfigField("String", "HOST", "\"https://shmr-finance.ru/api/\"")
        }
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.androidx.tracing.ktx)
}