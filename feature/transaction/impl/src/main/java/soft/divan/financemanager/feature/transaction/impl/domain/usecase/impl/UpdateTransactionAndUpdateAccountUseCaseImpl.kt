package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.rollbackOnError
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

class UpdateTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : UpdateTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем аккаунт
            val account = accountRepository.getById(transaction.accountLocalId).rollbackOnError()

            // 2. Получаем старую транзакцию (чтобы знать, какую сумму откатывать)
            val oldTransaction = transactionRepository.getById(transaction.id).rollbackOnError()

            // 3. Сначала откатываем старую транзакцию, затем применяем новую
            val revertedBalance = AccountBalanceCalculator.calculate(
                currentBalance = account.balance,
                transactionAmount = oldTransaction.amount,
                type = oldTransaction.type,
                isReverting = true
            )

            val newBalance = AccountBalanceCalculator.calculate(
                currentBalance = revertedBalance,
                transactionAmount = transaction.amount,
                type = transaction.type
            )

            // 4. Обновляем баланс аккаунта
            accountRepository.update(account.copy(balance = newBalance)).rollbackOnError()

            // 5. Обновляем саму транзакцию
            transactionRepository.update(transaction).rollbackOnError()

            DomainResult.Success(Unit)
        }
    }
}
