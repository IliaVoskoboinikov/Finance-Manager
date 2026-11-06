package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.transaction.rollbackOnError
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

/**
 * Реализация [DeleteTransactionAndUpdateAccountUseCase]: операция читается по id внутри той же
 * транзакции БД, что и откат баланса с удалением, — ошибка на любом шаге отменяет обе записи.
 */
class DeleteTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : DeleteTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transactionId: String): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем сохранённую транзакцию и её счёт
            val stored = transactionRepository.getById(transactionId).rollbackOnError()
            val account = accountRepository.getById(stored.accountLocalId).rollbackOnError()

            // 2. Пересчитываем баланс (откатываем сумму транзакции)
            val newBalance = AccountBalanceCalculator.calculate(
                currentBalance = account.balance,
                transactionAmount = stored.amount,
                type = stored.type,
                isReverting = true
            )

            // 3. Обновляем баланс только локально: сервер сам откатит сумму при
            // DELETE /transaction, PUT /account привёл бы к двойному применению.
            accountRepository.updateBalanceLocal(account.id, newBalance).rollbackOnError()

            // 4. Удаляем транзакцию
            transactionRepository.delete(stored.id).rollbackOnError()

            DomainResult.Success(Unit)
        }
    }
}
