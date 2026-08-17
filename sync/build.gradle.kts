plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.common)

    ksp(libs.hilt.ext.compiler)
    // ic_sync_notification.xml ссылается на ?attr/colorControlNormal из appcompat. Раньше
    // атрибут доезжал транзитивно через :app, но модулям, которые линкуют ресурсы :sync
    // самостоятельно (unit-тесты фич с includeAndroidResources), его не хватало.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.hilt.ext.work)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Robolectric-тесты воркеров и уведомлений
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
}
