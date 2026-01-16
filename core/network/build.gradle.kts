import java.util.Properties

plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

fun Project.getSecret(name: String): String {
    System.getenv(name)?.let { return it }

    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use(properties::load)
        properties.getProperty(name)?.let { return it }
    }

    error("Secret '$name' is not defined")
}

val apiToken: String = project.getSecret("api.token")

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