plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

// Типизированный DSL, а не `android {}`: в AGP 9 старый аксессор падает на приведении типа
// source set'а библиотеки.
extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    // Инструментальные тесты — на настоящем SQLCipher: его нативная библиотека в JVM не грузится
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Шифрование файла базы: Room открывает его через SupportOpenHelperFactory SQLCipher.
    implementation(libs.sqlcipher.android)

    // Robolectric-тесты DAO на in-memory Room
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
