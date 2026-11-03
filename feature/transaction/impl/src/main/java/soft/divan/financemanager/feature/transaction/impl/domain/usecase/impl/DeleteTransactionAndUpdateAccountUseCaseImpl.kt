package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.transaction.rollbackOnError
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

/**
 * Удаляет операцию и откатывает её сумму на счёте.
 *
 * Из [Transaction] берётся только id: откатывается **сохранённая** версия. Экран передаёт свою
 * копию, а в ней могут быть несохранённые правки — другой счёт или сумма увели бы откат не туда.
 */
class DeleteTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : DeleteTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем сохранённую транзакцию и её счёт
            val stored = transactionRepository.getById(transaction.id).rollbackOnError()
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
