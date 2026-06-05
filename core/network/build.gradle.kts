import java.util.Properties

plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

fun Project.getSecret(name: String): String {
    val env = System.getenv(name)
    if (env != null) return env

    val localPropertiesFile = rootProject.file("local.properties")
    var secret = ""
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use(properties::load)
        secret = properties.getProperty(name).orEmpty()
    }

    return secret
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
