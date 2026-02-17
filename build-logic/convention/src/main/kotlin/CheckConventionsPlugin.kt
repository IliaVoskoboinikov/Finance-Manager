import ModuleType.isApp
import ModuleType.isCore
import ModuleType.isFeature
import ModuleType.isFeatureApi
import ModuleType.isFeatureImpl
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Gradle-плагин для проверки соблюдения архитектурных конвенций проекта.
 *
 * Основные правила:
 * 1. Core-модули должны применять либо `soft.divan.core`, либо `soft.divan.jvm.library`, но не оба.
 * 2. Feature-модули разделяются на API и Impl, и не могут применять оба плагина одновременно.
 * 3. App-модуль должен применять плагин `soft.divan.android.app`.
 * 4. Проверка зависимостей модулей:
 *    - Core не может зависеть от Feature
 *    - Feature API не может зависеть от Feature Impl
 *    - Feature Impl не может зависеть от Feature Impl
 */
class CheckConventionsPlugin : Plugin<Project> {

    private val pluginChecks = PluginChecks()
    private val dependencyChecks = DependencyChecks()

    /**
     * Основная точка входа плагина. Итерирует все подпроекты и выполняет проверки:
     * - Core-модули
     * - Feature-модули
     * - App-модуль
     * - Архитектурные зависимости
     *
     * @param project Корневой проект, к которому применяется плагин
     */
    override fun apply(project: Project) {
        project.gradle.projectsEvaluated {
            project.rootProject.allprojects.forEach { child ->
                when {
                    child.isCore() -> pluginChecks.checkCoreModule(child)
                    child.isFeature() -> pluginChecks.checkFeatureModule(child)
                    child.isApp() -> pluginChecks.checkAppModule(child)
                }

                dependencyChecks.validateArchitecture(child)
            }
        }
    }
}

/**
 * Утилиты для определения типа модуля по пути.
 */
object ModuleType {

    /** Проверяет, является ли модуль core. */
    fun Project.isCore() = path.startsWith(":core:")

    /** Проверяет, является ли модуль feature (API или Impl). */
    fun Project.isFeature() = path.startsWith(":feature:")

    /** Проверяет, является ли модуль feature API. */
    fun Project.isFeatureApi() = isFeature() && path.endsWith(":api")

    /** Проверяет, является ли модуль feature Impl. */
    fun Project.isFeatureImpl() = isFeature() && path.endsWith(":impl")

    /** Проверяет, является ли модуль app. */
    fun Project.isApp() = path == ":app"
}

/**
 * Класс для проверки правильного применения плагинов в модулях.
 */
class PluginChecks {

    /**
     * Проверяет корректность применения плагинов в core-модуле.
     * Правила:
     * - Должен применяться хотя бы один плагин: `soft.divan.core` или `soft.divan.jvm.library`
     * - Нельзя применять оба плагина одновременно
     *
     * @param project Core-модуль для проверки
     * @throws GradleException при нарушении правил
     */
    fun checkCoreModule(project: Project) {
        val hasAndroidCore = project.pluginManager.hasPlugin("soft.divan.core")
        val hasJvmCore = project.pluginManager.hasPlugin("soft.divan.jvm.library")

        if (!hasAndroidCore && !hasJvmCore) {
            throw violation(
                project,
                "Core modules must apply one of: soft.divan.core or soft.divan.jvm.library"
            )
        }

        if (hasAndroidCore && hasJvmCore) {
            throw violation(project, "Core module cannot apply both Android and JVM conventions")
        }
    }

    /**
     * Проверяет корректность применения плагинов в feature-модуле.
     * Правила:
     * - Feature API плагин применён только в модулях с суффиксом `:api`
     * - Feature Impl плагин применён только в модулях с суффиксом `:impl`
     * - Нельзя применять оба плагина одновременно
     *
     * @param project Feature-модуль для проверки
     * @throws GradleException при нарушении правил
     */
    fun checkFeatureModule(project: Project) {
        val hasApi = project.pluginManager.hasPlugin("soft.divan.feature.api")
        val hasImpl = project.pluginManager.hasPlugin("soft.divan.feature.impl")

        val violationMessage = when {
            hasApi && hasImpl ->
                "Feature module cannot apply both api and impl plugins"

            hasApi && !project.isFeatureApi() ->
                "Feature API plugin applied to wrong module. Expected suffix :api"

            hasImpl && !project.isFeatureImpl() ->
                "Feature Impl plugin applied to wrong module. Expected suffix :impl"

            else -> null
        }

        violationMessage?.let { throw violation(project, it) }
    }

    /**
     * Проверяет, что app-модуль применяет обязательный плагин `soft.divan.android.app`.
     *
     * @param project App-модуль для проверки
     * @throws GradleException если плагин не применён
     */
    fun checkAppModule(project: Project) {
        val requiredPlugin = "soft.divan.android.app"
        if (!project.pluginManager.hasPlugin(requiredPlugin)) {
            throw violation(project, "App module must apply soft.divan.android.app")
        }
    }

    /** Формирует исключение GradleException с сообщением о нарушении плагина. */
    private fun violation(project: Project, message: String) =
        GradleException("Module: ${project.path}\n$message")
}

/**
 * Класс для проверки соблюдения архитектурных зависимостей между модулями.
 */
class DependencyChecks {

    /**
     * Проверяет соблюдение архитектурных границ между модулями.
     * Правила:
     * - Core не может зависеть от Feature
     * - Feature API не может зависеть от Feature Impl
     * - Feature Impl не может зависеть от других Feature Impl
     *
     * @param project Модуль для проверки зависимостей
     * @throws GradleException при нарушении правил
     */
    fun validateArchitecture(project: Project) {
        val configurationsToCheck = setOf("api", "implementation")
        project.configurations.matching { it.name in configurationsToCheck }
            .forEach { configuration ->
                configuration.dependencies
                    .filterIsInstance<ProjectDependency>()
                    .forEach { dependency ->
                        val depProject = dependency.dependencyProject
                        val msg = when {
                            project.isFeatureImpl() && depProject.isFeatureImpl() ->
                                "Feature impl modules must NOT depend on other feature impl modules"

                            project.isFeatureApi() && depProject.isFeatureImpl() ->
                                "Feature api modules must NOT depend on feature impl modules"

                            project.isCore() && depProject.isFeature() ->
                                "Core modules must NOT depend on feature modules"

                            else -> null
                        }
                        msg?.let {
                            throw GradleException(
                                "From: ${project.path}\nTo: ${depProject.path}\nRule: $it"
                            )
                        }
                    }
            }
    }
}
