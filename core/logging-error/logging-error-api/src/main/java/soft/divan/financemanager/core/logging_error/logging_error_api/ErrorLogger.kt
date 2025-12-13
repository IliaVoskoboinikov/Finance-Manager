package soft.divan.financemanager.core.logging_error.logging_error_api

interface ErrorLogger {
    fun recordError(message: String? = null)
}