import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

lint {
    htmlReport = true
    textReport = true
    absolutePaths = false
    ignoreTestSources = true
    checkDependencies = true
    sarifReport = true
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.kotlin.stdlib)
    testImplementation(libs.lint.tests)
    testImplementation(libs.lint.cli)
    testImplementation(libs.junit)
}
