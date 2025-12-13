package soft.divan.financemanager.core.domain.util

sealed interface ErrorType {
    data object Network : ErrorType
    data object Unauthorized : ErrorType
    data object Server : ErrorType
    data object NoData : ErrorType
    data object Unknown : ErrorType
    data object LocalDb : ErrorType
}
