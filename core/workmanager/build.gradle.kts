plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    // api, а не implementation: DelegatingWorker и delegatedData() — часть публичного
    // контракта модуля, поэтому WorkManager и hilt-work должны быть видны потребителям
    // (:sync, :core:notifications), иначе им пришлось бы дублировать эти зависимости.
    api(libs.androidx.work.ktx)
    api(libs.hilt.ext.work)

    ksp(libs.hilt.ext.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
}
