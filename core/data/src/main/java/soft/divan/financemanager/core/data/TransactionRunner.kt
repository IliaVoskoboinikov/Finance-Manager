package soft.divan.financemanager.core.data

import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult

/**
 * Выполняет составную локальную операцию атомарно — «всё или ничего».
 *
 * Используется use case'ами, которым нужно изменить несколько сущностей согласованно
 * (например, создать транзакцию и обновить баланс счёта). Абстракция позволяет
 * use case'ам не зависеть от Room; единственная реализация — [RoomTransactionRunner].
 *
 * Типичный паттерн использования:
 * ```
 * transactionRunner.runInTransaction {
 *     val account = accountRepository.getById(id).rollbackOnError()
 *     accountRepository.updateBalanceLocal(account.id, newBalance).rollbackOnError()
 *     transactionRepository.create(transaction).rollbackOnError()
 *     DomainResult.Success(Unit)
 * }
 * ```
 */
interface TransactionRunner {
    /**
     * Выполняет [block] в БД-транзакции.
     *
     * Контракт:
     * - откат — только через исключение; для [DomainResult] используйте [rollbackOnError],
     *   тогда [runInTransaction] вернёт `DomainResult.Failure` с исходной ошибкой
     *   (поэтому [T] в этом случае обязан быть `DomainResult<*>`);
     * - сетевые пуши, запущенные внутри блока через `AppCoroutineContext.launchSync`,
     *   откладываются до успешного commit и отбрасываются при rollback
     *   (механизм и ограничения: docs/post-commit-sync.md);
     * - вложенные вызовы [runInTransaction] не поддерживаются — отложенные пуши
     *   внутреннего блока задиспатчатся до commit внешней транзакции.
     */
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

/**
 * Служебное исключение для отката транзакции: переносит [DomainError] из места сбоя
 * наружу, где [RoomTransactionRunner] превращает его обратно в `DomainResult.Failure`.
 * Не предназначено для обработки за пределами [TransactionRunner].
 */
class TransactionRollbackException(val error: DomainError) : Exception()

/**
 * Разворачивает [DomainResult] внутри [TransactionRunner.runInTransaction]:
 * `Success` → данные, `Failure` → [TransactionRollbackException] (откат всей транзакции,
 * ошибка вернётся вызывающему как `DomainResult.Failure`).
 */
fun <T> DomainResult<T>.rollbackOnError(): T {
    return when (this) {
        is DomainResult.Success -> data
        is DomainResult.Failure -> throw TransactionRollbackException(error)
    }
}
