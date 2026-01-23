import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.kotlin.stdlib)
    testImplementation(libs.lint.tests)
    testImplementation(libs.lint.cli)
    testImplementation(libs.junit)
}