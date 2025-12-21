package soft.divan.financemanager.core.domain.result

import soft.divan.financemanager.core.domain.error.DomainError

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

inline fun <T, R> DomainResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (DomainError) -> R
): R = when (this) {
    is DomainResult.Success -> onSuccess(data)
    is DomainResult.Failure -> onFailure(error)
}
