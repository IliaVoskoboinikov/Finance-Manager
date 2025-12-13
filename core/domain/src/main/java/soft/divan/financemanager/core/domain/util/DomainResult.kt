package soft.divan.financemanager.core.domain.util

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val reason: ErrorType, val message: String? = null) : DomainResult<Nothing>
}
