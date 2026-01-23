import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.graph) apply false
    alias(libs.plugins.build.time.tracker) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.lint)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    group = "verification"
    description = "Run Detekt on all modules (aggregated)"

    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = true

    config.setFrom(file(File(rootDir, "config/detekt/detekt.yml")))
    setSource(files(rootDir))

    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
    exclude("**/.gradle/**")
    exclude("**/generated/**")

    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
