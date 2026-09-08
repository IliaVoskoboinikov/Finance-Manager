package soft.divan.financemanager.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Архитектурные тесты уровня классов и файлов.
 *
 * Дополняют `:app:assertModuleGraph` (рёбра между модулями) и `CheckConventionsPlugin`
 * (какие convention-плагины применены): здесь проверяется, что конкретный класс лежит
 * в правильном пакете, правильно назван и правильно аннотирован.
 *
 */
class ArchitectureKonsistTest {

    /**
     * Страховка от «зелёного» прогона на пустом scope: если Konsist перестанет
     * находить исходники (переехали модули, изменился layout), все остальные
     * правила пройдут молча, ничего не проверив.
     */
    @Test
    fun `production scope is not empty`() {
        assertThat(productionScope.files).hasSizeGreaterThan(MIN_EXPECTED_PRODUCTION_FILES)
    }

    @Test
    fun `use case classes reside in domain package`() {
        productionScope
            .classes()
            .withNameEndingWith(USE_CASE_SUFFIX)
            .assertTrue { it.resideInPackage("..domain..") }
    }

    @Test
    fun `repository interfaces reside in domain package`() {
        productionScope
            .interfaces()
            .withNameEndingWith(REPOSITORY_SUFFIX)
            .assertTrue { it.resideInPackage("..domain..") }
    }

    /**
     * Реализация репозитория — часть data-слоя. Если `*RepositoryImpl` оказался
     * в `domain`, значит слой домена потянул за собой Room/Retrofit.
     */
    @Test
    fun `repository implementations do not reside in domain package`() {
        productionScope
            .classes()
            .withNameEndingWith(REPOSITORY_IMPL_SUFFIX)
            .assertTrue { !it.resideInPackage("..domain..") }
    }

    /**
     * Room-сущности не должны покидать `:core:database` — см. «Layer isolation»
     */
    @Test
    fun `room entities reside only in core database module`() {
        productionScope
            .classes()
            .filter { it.hasAnnotationWithName(ROOM_ENTITY_ANNOTATION) }
            .assertTrue { it.resideInPackage("..core.database..") }
    }

    /** DTO — граница сети, живут в `..dto..` и наружу не выходят. */
    @Test
    fun `dto classes reside in dto package`() {
        productionScope
            .classes()
            .withNameEndingWith(DTO_SUFFIX)
            .assertTrue { it.resideInPackage("..dto..") }
    }

    /** ViewModel создаётся только через Hilt — ручных конструкторов в проекте нет. */
    @Test
    fun `view models are annotated with HiltViewModel`() {
        productionScope
            .classes()
            .withNameEndingWith(VIEW_MODEL_SUFFIX)
            .assertTrue { it.hasAnnotationWithName(HILT_VIEW_MODEL_ANNOTATION) }
    }

    /**
     * Дублирует кастомный lint-чекер `OldDate` из модуля `:lint`, но срабатывает
     * в обычном `test`-таске — то есть на пару минут раньше в пайплайне.
     */
    @Test
    fun `legacy date api is not used`() {
        productionScope
            .files
            .assertTrue { !it.hasImportWithName(FORBIDDEN_DATE_IMPORTS) }
    }

    private companion object {
        const val USE_CASE_SUFFIX = "UseCase"
        const val REPOSITORY_SUFFIX = "Repository"
        const val REPOSITORY_IMPL_SUFFIX = "RepositoryImpl"
        const val DTO_SUFFIX = "Dto"
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val ROOM_ENTITY_ANNOTATION = "Entity"
        const val HILT_VIEW_MODEL_ANNOTATION = "HiltViewModel"

        /** Заведомо заниженная граница: в проекте ~500 файлов продуктового кода. */
        const val MIN_EXPECTED_PRODUCTION_FILES = 100

        val FORBIDDEN_DATE_IMPORTS = listOf("java.util.Date", "java.util.Calendar")

        /**
         * Продуктовый код всех модулей.
         *
         * Скрытые каталоги отброшены: в них лежат служебные данные сборки и
         * инструментов — в том числе рабочие копии других веток, чьи исходники
         * Konsist иначе посчитал бы частью проекта и проверил бы дважды.
         */
        val productionScope = Konsist
            .scopeFromProduction()
            .slice { file -> file.projectPath.split("/").none { it.startsWith(".") } }
    }
}
