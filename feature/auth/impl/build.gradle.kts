plugins {
    alias(libs.plugins.soft.divan.feature.impl)
    alias(libs.plugins.soft.divan.hilt)
}

android {
    // Плагин navgraph включает testOptions.unitTests.isIncludeAndroidResources (нужно
    // рендереру превью), из-за чего AGP собирает манифест unit-тестов и мержит в него
    // манифест Yandex ID SDK с плейсхолдерами ${YANDEX_CLIENT_ID}. Реальное значение
    // подставляет :app при финальном мерже; здесь нужна лишь заглушка, чтобы мерж тестового
    // манифеста не падал. В AAR она не попадает: собственный манифест модуля плейсхолдеров
    // не содержит.
    defaultConfig {
        manifestPlaceholders["YANDEX_CLIENT_ID"] = ""
        manifestPlaceholders["YANDEX_OAUTH_HOST"] = "oauth.yandex.ru"
    }
}

dependencies {
    implementation(projects.feature.auth.api)
    implementation(projects.core.uikit)
    implementation(projects.core.loggingError)
    implementation(projects.core.auth)
    implementation(projects.core.domain)
    implementation(projects.sync)

    implementation(libs.yandex.authsdk)
}
