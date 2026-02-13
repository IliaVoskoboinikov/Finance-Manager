package soft.divan.financemanager

import Const
import org.gradle.api.Project

fun generateNamespace(project: Project): String {
    val root = Const.NAMESPACE
    val path = project.path

    return path.split(":")
        .drop(1)
        .joinToString(separator = ".") { it.replace("-", "_") }
        .let { if (it.isEmpty()) root else "$root.$it" }
}
