package soft.divan.financemanager.core.domain.result

import soft.divan.financemanager.core.domain.error.DomainError

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}