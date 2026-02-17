package soft.divan.financemanager.core.loggingerror

interface ErrorLogger {
    fun recordError(message: String? = null)
}
