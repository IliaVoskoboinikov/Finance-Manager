import java.util.Properties

plugins {
    alias(libs.plugins.soft.divan.android.app)
}

/**
 * Читает секрет из переменной окружения (CI) или из local.properties (локально).
 * Зеркалит подход из :core:network.
 */
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

// client_id приложения в Яндекс OAuth. Задаётся только через local.properties / CI-секрет
// YANDEX_CLIENT_ID (в репозиторий не коммитится — как API_TOKEN). Значение публично по
// природе OAuth (SDK кладёт его в meta-data манифеста, оно видно в схеме редиректа
// yx<client_id>), но в исходниках его не держим, чтобы не смешивать с конфигом сборки.
val yandexClientId: String = getSecret("YANDEX_CLIENT_ID")

android {
    namespace = Const.NAMESPACE
    defaultConfig {
        applicationId = Const.NAMESPACE

        // Плейсхолдеры подставляются в meta-data и deep-link из манифеста Yandex ID SDK.
        // Финальный merge SDK-манифеста происходит в :app, поэтому значения задаются здесь.
        manifestPlaceholders["YANDEX_CLIENT_ID"] = yandexClientId
        manifestPlaceholders["YANDEX_OAUTH_HOST"] = "oauth.yandex.ru"
    }
    testOptions {
        unitTests {
            // Robolectric-тестам БД нужен доступ к assets (prepackaged category_db.db)
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.sync)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.loggingError)
    implementation(projects.core.auth)
    implementation(projects.core.notifications)

    implementation(projects.feature.category.api)
    implementation(projects.feature.category.impl)

    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)

    implementation(projects.feature.myAccounts.api)
    implementation(projects.feature.myAccounts.impl)

    implementation(projects.feature.transactionsToday.api)
    implementation(projects.feature.transactionsToday.impl)

    implementation(projects.feature.splashScreen.api)
    implementation(projects.feature.splashScreen.impl)

    implementation(projects.feature.transaction.api)
    implementation(projects.feature.transaction.impl)

    implementation(projects.feature.security.api)
    implementation(projects.feature.security.impl)

    implementation(projects.feature.designApp.api)
    implementation(projects.feature.designApp.impl)

    implementation(projects.feature.analysis.api)
    implementation(projects.feature.analysis.impl)

    implementation(projects.feature.history.api)
    implementation(projects.feature.history.impl)

    implementation(projects.feature.account.api)
    implementation(projects.feature.account.impl)

    implementation(projects.feature.haptics.api)
    implementation(projects.feature.haptics.impl)

    implementation(projects.feature.sounds.api)
    implementation(projects.feature.sounds.impl)

    implementation(projects.feature.languages.api)
    implementation(projects.feature.languages.impl)

    implementation(projects.feature.synchronization.api)
    implementation(projects.feature.synchronization.impl)

    implementation(projects.feature.auth.api)
    implementation(projects.feature.auth.impl)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.runtime)
}

tasks.register("printVersionName") {
    doLast {
        println(Const.VERSION_NAME)
    }
}

// Экспорт карты навигации в docs/graphs/nav_graph/ — коммитим статичные PNG и самодостаточные
// интерактивные HTML (миниатюры вшиты в base64), чтобы граф и галерея превью были видны прямо
// в документации (docs/nav-graph.md). Кладём и сам граф (nav-graph), и галерею всех @Preview
// (preview-gallery). Запускать вручную после осознанного изменения графа:
//   ./gradlew :app:exportNavGraphToDocs
//
// Замечание по путям: export-задачи плагина пишут в build/navgraph/ и build/navgallery/
// (без подчёркиваний), а целевая папка документации — docs/graphs/nav_graph/.
tasks.register<Copy>("exportNavGraphToDocs") {
    group = "navgraph"
    description = "Copies nav-graph + preview-gallery (png/html) into docs/graphs/nav_graph/"
    dependsOn(
        "exportNavGraphImage",
        "exportNavGraphHtml",
        "exportPreviewGalleryImage",
        "exportPreviewGalleryHtml"
    )
    from(layout.buildDirectory.dir("navgraph")) {
        include("nav-graph.png", "nav-graph.html")
    }
    from(layout.buildDirectory.dir("navgallery")) {
        include("preview-gallery.png", "preview-gallery.html")
    }
    into(rootProject.layout.projectDirectory.dir("docs/graphs/nav_graph"))
}
