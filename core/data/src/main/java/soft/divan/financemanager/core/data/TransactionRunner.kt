package soft.divan.financemanager.core.data

import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult

interface TransactionRunner {
    /**
     * Выполняет блок в транзакции. Для отката нужно выбросить исключение.
     */
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

/**
 * Исключение для внутреннего использования, чтобы откатить транзакцию Room
 * и передать DomainError наружу.
 */
class TransactionRollbackException(val error: DomainError) : Exception()

/**
 * Вспомогательная функция для использования внутри [TransactionRunner.runInTransaction].
 * Если результат - ошибка, выбрасывает [TransactionRollbackException], что приводит к откату транзакции.
 */
fun <T> DomainResult<T>.rollbackOnError(): T {
    return when (this) {
        is DomainResult.Success -> data
        is DomainResult.Failure -> throw TransactionRollbackException(error)
    }
}
