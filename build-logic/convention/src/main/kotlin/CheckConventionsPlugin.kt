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

    override fun apply(project: Project) {
        project.gradle.projectsEvaluated {
            project.rootProject.allprojects.forEach { child ->
                when {
                    child.isCoreModule() -> checkCoreModule(child)
                    child.isFeatureModule() -> checkFeatureModule(child)
                    child.isAppModule() -> checkAppModule(child)
                }

                validateArchitecture(child)
            }
        }
    }

    /** Определение типа модуля ======= */
    private fun Project.isCoreModule() = path.startsWith(":core:")
    private fun Project.isFeatureModule() = path.startsWith(":feature:")
    private fun Project.isFeatureApi() = isFeatureModule() && path.endsWith(":api")
    private fun Project.isFeatureImpl() = isFeatureModule() && path.endsWith(":impl")
    private fun Project.isAppModule() = path == ":app"

    /**
     * Проверяет корректность применения плагинов в core-модуле.
     * Правила:
     * - Должен применяться хотя бы один плагин: `soft.divan.core` или `soft.divan.jvm.library`
     * - Нельзя применять оба плагина одновременно
     *
     * @param project Core-модуль для проверки
     * @throws GradleException при нарушении правил
     */
    private fun checkCoreModule(project: Project) {
        val hasAndroidCore = project.pluginManager.hasPlugin("soft.divan.core")
        val hasJvmCore = project.pluginManager.hasPlugin("soft.divan.jvm.library")

        when {
            !hasAndroidCore && !hasJvmCore -> throw GradleException(
                """
                Core modules must apply one of:
                - soft.divan.core (Android core)
                - soft.divan.jvm.library (JVM core)
                Module: ${project.path}
                """.trimIndent()
            )

            hasAndroidCore && hasJvmCore -> throw GradleException(
                """
                Core module cannot apply both Android and JVM conventions.
                Module: ${project.path}
                """.trimIndent()
            )
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
    private fun checkFeatureModule(project: Project) {
        val hasApi = project.pluginManager.hasPlugin("soft.divan.feature.api")
        val hasImpl = project.pluginManager.hasPlugin("soft.divan.feature.impl")

        if (!hasApi && !hasImpl) return

        when {
            hasApi && !project.isFeatureApi() -> throw GradleException(
                """
                Feature API plugin applied to wrong module.
                Module: ${project.path}
                Expected suffix: :api
                """.trimIndent()
            )

            hasImpl && !project.isFeatureImpl() -> throw GradleException(
                """
                Feature Impl plugin applied to wrong module.
                Module: ${project.path}
                Expected suffix: :impl
                """.trimIndent()
            )

            hasApi && hasImpl -> throw GradleException(
                """
                Feature module cannot apply both api and impl plugins.
                Module: ${project.path}
                """.trimIndent()
            )
        }
    }

    /**
     * Проверяет, что app-модуль применяет обязательный плагин `soft.divan.android.app`.
     * Выбрано фиксированное значение requiredPlugin и сообщения для всех app-модулей.
     *
     * @param project App-модуль для проверки
     * @throws GradleException если плагин не применён
     */
    private fun checkAppModule(project: Project) {
        val requiredPlugin = "soft.divan.android.app"
        val message = "App module must apply soft.divan.android.app"
        if (!project.pluginManager.hasPlugin(requiredPlugin)) {
            throw GradleException(
                """
                $message
                Module: ${project.path}
                Required plugin: $requiredPlugin
                """.trimIndent()
            )
        }
    }

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
    private fun validateArchitecture(project: Project) {
        val configurationsToCheck = setOf("api", "implementation")
        project.configurations.matching { it.name in configurationsToCheck }
            .forEach { configuration ->
                configuration.dependencies
                    .filterIsInstance<ProjectDependency>()
                    .forEach { dependency ->
                        val depProject = dependency.dependencyProject

                        when {
                            project.isFeatureImpl() && depProject.isFeatureImpl() ->
                                throw violation(
                                    project.path,
                                    depProject.path,
                                    "Feature impl modules must NOT depend on other feature impl modules"
                                )

                            project.isFeatureApi() && depProject.isFeatureImpl() ->
                                throw violation(
                                    project.path,
                                    depProject.path,
                                    "Feature api modules must NOT depend on feature impl modules"
                                )

                            project.isCoreModule() && depProject.isFeatureModule() ->
                                throw violation(
                                    project.path,
                                    depProject.path,
                                    "Core modules must NOT depend on feature modules"
                                )
                        }
                    }
            }
    }

    /**
     * Формирует исключение GradleException с подробной информацией о нарушении архитектуры.
     *
     * @param from Модуль, который содержит зависимость
     * @param to Модуль, на который есть зависимость
     * @param message Описание правила, которое нарушено
     * @return GradleException с детальной информацией
     */
    private fun violation(from: String, to: String, message: String): GradleException =
        GradleException(
            """
            ❌ Architecture violation detected
            Rule: $message
            From: $from
            To:   $to
            Fix the dependency to respect module boundaries.
            """.trimIndent()
        )
}
