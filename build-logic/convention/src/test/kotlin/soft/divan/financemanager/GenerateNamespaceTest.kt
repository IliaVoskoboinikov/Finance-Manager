package soft.divan.financemanager

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GenerateNamespaceTest {

    private fun projectAt(path: String): Project {
        var project = ProjectBuilder.builder().withName("root").build()
        if (path == ":") return project
        path.removePrefix(":").split(":").forEach { name ->
            project = ProjectBuilder.builder().withName(name).withParent(project).build()
        }
        return project
    }

    @Test
    fun `core module path maps to dotted namespace`() {
        assertThat(generateNamespace(projectAt(":core:data")))
            .isEqualTo("soft.divan.financemanager.core.data")
    }

    @Test
    fun `feature impl path maps to nested namespace`() {
        assertThat(generateNamespace(projectAt(":feature:auth:impl")))
            .isEqualTo("soft.divan.financemanager.feature.auth.impl")
    }

    @Test
    fun `dashes in module names become underscores`() {
        assertThat(generateNamespace(projectAt(":feature:my-accounts:impl")))
            .isEqualTo("soft.divan.financemanager.feature.my_accounts.impl")
    }

    @Test
    fun `root project falls back to base namespace`() {
        assertThat(generateNamespace(projectAt(":"))).isEqualTo("soft.divan.financemanager")
    }
}
