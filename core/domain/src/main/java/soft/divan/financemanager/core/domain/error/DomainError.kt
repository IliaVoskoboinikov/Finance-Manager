package soft.divan.financemanager.core.domain.error

sealed interface DomainError : AppError {

    data object NoData : DomainError {
        override val cause: Throwable? = null
    }

    data object Unauthorized : DomainError {
        override val cause: Throwable? = null
    }

    data object OperationNotAllowed : DomainError {
        override val cause: Throwable? = null
    }

    data object NetworkUnavailable : DomainError {
        override val cause: Throwable? = null
    }

    data class Unknown(
        override val cause: Throwable?
    ) : DomainError
}
