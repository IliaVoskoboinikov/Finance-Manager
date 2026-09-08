package soft.divan.financemanager

import com.github.skydoves.navgraph.gradle.NavGraphExtension
import com.github.skydoves.navgraph.gradle.RenderBackend
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure

/**
 * Подключает плагин `navgraph` — статическое извлечение графа навигации из аннотаций
 * `@NavDestination` / `@NavEdge` / `@NavPreview` (см. `docs/nav-graph.md`).
 *
 * Плагин сам добавляет модулю зависимость на annotations и свой KSP-процессор, а результат
 * (`nav-graph.json`) агрегируется в `:app`.
 *
 * Бэкенд рендера превью фиксируется на Layoutlib. Значение по умолчанию (`AUTO`) вдобавок
 * генерирует в каждый модуль Robolectric-тест `NavGraphRobolectricRenderTest`, который
 * попадает в `testDebugUnitTest` — то есть в CI-джобу с тестами, где ему не место.
 *
 * Сам плагин при подключении включает модулю `testOptions.unitTests.includeAndroidResources`:
 * рендереру нужен `apk_for_local_test` со скомпилированными ресурсами. Отключать эту
 * настройку нельзя — без неё превью не рендерятся (см. `docs/nav-graph.md`).
 */
fun Project.configureNavGraph() {
    applyPlugin("navgraph")

    extensions.configure<NavGraphExtension> {
        renderBackend.set(RenderBackend.LAYOUTLIB)
        galleryRenderBackend.set(RenderBackend.LAYOUTLIB)
    }

    // Побочный эффект includeAndroidResources: задача unit-тестов перестаёт быть NO-SOURCE
    // даже в модуле без единого теста, и Gradle 9 валит её проверкой failOnNoDiscoveredTests.
    // Отключаем проверку только там, где своих тестов нет; в остальных модулях она остаётся
    // рабочей защитой от неверных фильтров.
    if (!file("src/test").exists()) {
        tasks.withType(Test::class.java).configureEach { failOnNoDiscoveredTests.set(false) }
    }
}
