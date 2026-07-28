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
        // ВАЖНО: все пути в Retrofit-интерфейсах уже содержат префикс "api/v1/..."
        // (см. AuthApiService и остальные *ApiService), поэтому HOST должен быть
        // корнем сервера без "/api". Иначе в URL получается двойной сегмент
        // ("/api/api/v1/...") и все сетевые запросы, включая login/register, падают с 404.
        release {
            isMinifyEnabled = false
            buildConfigField("String", "HOST", "\"http://yourflow.pro/\"")
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

    // Robolectric-тест ConnectivityManagerNetworkMonitor
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
