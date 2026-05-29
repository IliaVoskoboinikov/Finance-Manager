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

    return "" // Сделаем пустую строку по умолчанию, так как теперь токен будет в SessionDataStore
}

val apiToken: String = project.getSecret("API_TOKEN")

android {
    defaultConfig {
        buildConfigField("String", "API_TOKEN", "\"$apiToken\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "HOST", "\"http://yourflow.pro/api/\"")
        }

        debug {
            buildConfigField("String", "HOST", "\"http://yourflow.pro/\"")
        }
    }
}

dependencies {
    implementation(projects.core.auth)
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.androidx.tracing.ktx)
}
