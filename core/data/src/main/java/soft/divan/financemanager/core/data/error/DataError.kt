package soft.divan.financemanager.core.data.error

import soft.divan.financemanager.core.domain.error.AppError

sealed interface DataError : AppError {

    data object Network : DataError {
        override val cause: Throwable? = null
    }

    data object Unauthorized : DataError {
        override val cause: Throwable? = null
    }

    data object Server : DataError {
        override val cause: Throwable? = null
    }

    data object NotFound : DataError {
        override val cause: Throwable? = null
    }

    data class LocalDb(
        override val cause: Throwable?
    ) : DataError

    data class Unknown(
        override val cause: Throwable?
    ) : DataError
}
