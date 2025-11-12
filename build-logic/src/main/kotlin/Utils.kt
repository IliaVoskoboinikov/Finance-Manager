import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

internal val Project.libs: LibrariesForLibs
    get() = (this as ExtensionAware).extensions.getByName("libs") as LibrariesForLibs

fun generateNamespace(project: Project): String {
    val root = Const.NAMESPACE
    val path = project.path

    return path.split(":")
        .drop(1)
        .joinToString(separator = ".") { it.replace("-", "_") }
        .let { if (it.isEmpty()) root else "$root.$it" }
}
