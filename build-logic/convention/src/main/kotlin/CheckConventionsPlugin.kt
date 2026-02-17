import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class CheckConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.gradle.projectsEvaluated {
            project.rootProject.allprojects.forEach { child ->

                when {
                    child.path.startsWith(":core:") -> {
                        checkCoreModule(child)
                    }

                    child.path.startsWith(":feature:") -> {
                        checkFeatureModule(child)
                    }

                    child.path == ":app" -> {
                        checkPlugin(
                            project = child,
                            requiredPlugin = "soft.divan.android.app",
                            message = "App module must apply soft.divan.android.app"
                        )
                    }
                }

                validateArchitecture(child)
            }
        }
    }

    private fun checkCoreModule(project: Project) {
        val hasAndroidCore = project.pluginManager.hasPlugin("soft.divan.core")
        val hasJvmCore = project.pluginManager.hasPlugin("soft.divan.jvm.library")

        if (!hasAndroidCore && !hasJvmCore) {
            throw GradleException(
                """
            Core modules must apply one of:
            
            - soft.divan.core (Android core)
            - soft.divan.jvm.library (JVM core)
            
            Module: ${project.path}
                """.trimIndent()
            )
        }

        if (hasAndroidCore && hasJvmCore) {
            throw GradleException(
                """
            Core module cannot apply both Android and JVM conventions.
            
            Module: ${project.path}
                """.trimIndent()
            )
        }
    }

    private fun checkFeatureModule(project: Project) {
        val hasApiPlugin =
            project.pluginManager.hasPlugin("soft.divan.feature.api")

        val hasImplPlugin =
            project.pluginManager.hasPlugin("soft.divan.feature.impl")

        if (!hasApiPlugin && !hasImplPlugin) return

        val isApiModule = project.path.endsWith(":api")
        val isImplModule = project.path.endsWith(":impl")

        when {
            hasApiPlugin && !isApiModule -> {
                throw GradleException(
                    """
                Feature API plugin applied to wrong module.
                
                Module: ${project.path}
                Expected suffix: :api
                    """.trimIndent()
                )
            }

            hasImplPlugin && !isImplModule -> {
                throw GradleException(
                    """
                Feature Impl plugin applied to wrong module.
                
                Module: ${project.path}
                Expected suffix: :impl
                    """.trimIndent()
                )
            }

            hasApiPlugin && hasImplPlugin -> {
                throw GradleException(
                    """
                Feature module cannot apply both api and impl plugins.
                
                Module: ${project.path}
                    """.trimIndent()
                )
            }
        }
    }

    private fun checkPlugin(
        project: Project,
        requiredPlugin: String,
        message: String
    ) {
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

    private fun validateArchitecture(project: Project) {
        val projectPath = project.path

        project.configurations
            .matching { it.name in setOf("api", "implementation") }
            .forEach { configuration ->

                configuration.dependencies
                    .filterIsInstance<ProjectDependency>()
                    .forEach { dependency ->

                        val dependencyPath = dependency.dependencyProject.path

                        when {
                            isFeatureImpl(projectPath) && isFeatureImpl(dependencyPath) -> {
                                throw violation(
                                    projectPath,
                                    dependencyPath,
                                    "Feature impl modules must NOT depend on other feature impl modules"
                                )
                            }

                            isFeatureApi(projectPath) && isFeatureImpl(dependencyPath) -> {
                                throw violation(
                                    projectPath,
                                    dependencyPath,
                                    "Feature api modules must NOT depend on feature impl modules"
                                )
                            }

                            isCore(projectPath) && isFeature(dependencyPath) -> {
                                throw violation(
                                    projectPath,
                                    dependencyPath,
                                    "Core modules must NOT depend on feature modules"
                                )
                            }
                        }
                    }
            }
    }

    private fun violation(
        from: String,
        to: String,
        message: String
    ): GradleException {
        return GradleException(
            """
            ❌ Architecture violation detected
            
            Rule: $message
            
            From: $from
            To:   $to
            
            Fix the dependency to respect module boundaries.
            """.trimIndent()
        )
    }

    private fun isFeature(path: String) =
        path.startsWith(":feature:")

    private fun isFeatureApi(path: String) =
        path.startsWith(":feature:") && path.endsWith(":api")

    private fun isFeatureImpl(path: String) =
        path.startsWith(":feature:") && path.endsWith(":impl")

    private fun isCore(path: String) =
        path.startsWith(":core:")
}
