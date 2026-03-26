package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.rollbackOnError
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

class DeleteTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : DeleteTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем аккаунт
            val account = accountRepository.getById(transaction.accountLocalId).rollbackOnError()

            // 2. Пересчитываем баланс (откатываем сумму транзакции)
            val newBalance = AccountBalanceCalculator.calculate(
                currentBalance = account.balance,
                transactionAmount = transaction.amount,
                type = transaction.type,
                isReverting = true
            )

            // 3. Обновляем баланс
            accountRepository.update(account.copy(balance = newBalance)).rollbackOnError()

            // 4. Удаляем транзакцию
            transactionRepository.delete(transaction.id).rollbackOnError()

            DomainResult.Success(Unit)
        }
    }
}
