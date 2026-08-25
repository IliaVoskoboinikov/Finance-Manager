import com.jraska.module.graph.assertion.GraphRulesExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.financemanager.applyPlugin

/**
 * Правила графа модулей, проверяемые задачей `:app:assertModuleGraph`.
 *
 * Дополняет [CheckConventionsPlugin], а не дублирует его: тот смотрит на прямые
 * зависимости конкретного модуля, здесь же ограничения задаются регулярками поверх
 * всего графа, плюс контролируется его высота.
 *
 * Высота графа — число рёбер в самой длинной цепочке зависимостей (не узлов). Чем она
 * больше, тем меньше модулей Gradle может собирать параллельно: каждый уровень ждёт
 * предыдущий. Текущая цепочка-рекордсмен из шести рёбер:
 * `:app → :feature:synchronization:impl → :sync → :core:data → :core:network → :core:auth → :core:common`.
 * Порог зафиксирован по факту, без запаса — новая зависимость, углубляющая граф,
 * уронит сборку и потребует осознанного решения.
 */
class ModuleGraphConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugin("graph")
            extensions.configure<GraphRulesExtension> {
                maxHeight = MAX_GRAPH_HEIGHT
                restricted = RESTRICTED_EDGES
                configurations = setOf("api", "implementation")
            }
        }
    }

    private companion object {
        const val MAX_GRAPH_HEIGHT = 6

        val RESTRICTED_EDGES = arrayOf(
            // Домен — чистый Kotlin: ни Room, ни Retrofit, ни Android-зависимостей.
            // Этого правила нет в CheckConventionsPlugin, а нарушить его легко.
            ":core:domain -X> :core:(data|database|network|auth|security)",

            // Хост навигации никому не виден: :app знает про всех, о нём — никто.
            ":core:.* -X> :app",
            ":feature:.* -X> :app",
            ":sync -X> :app",

            // :sync — фоновая синхронизация поверх data-слоя. Фичи ему не нужны:
            // иначе WorkManager-воркер потянет за собой презентационный слой.
            ":sync -X> :feature:.*",

            // Границы, которые проверяет и CheckConventionsPlugin. Здесь они записаны
            // декларативно и попадают в отчёт задачи, а не только в текст исключения.
            ":core:.* -X> :feature:.*",
            ":feature:.*:api -X> :feature:.*:impl",
        )
    }
}
