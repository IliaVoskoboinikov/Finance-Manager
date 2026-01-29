package soft.divan.financemanager.core.featureapi

@JvmInline
value class RouteScope(private val value: String) {

    fun route(path: String = ""): String =
        when {
            value.isEmpty() -> path
            path.isEmpty() -> value
            else -> "$value/$path"
        }

    fun child(segment: String): RouteScope =
        RouteScope(route(segment))
}
