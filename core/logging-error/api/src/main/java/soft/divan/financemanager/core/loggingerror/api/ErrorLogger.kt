package soft.divan.financemanager.core.loggingerror.api

interface ErrorLogger {
    fun recordError(message: String? = null)
}
