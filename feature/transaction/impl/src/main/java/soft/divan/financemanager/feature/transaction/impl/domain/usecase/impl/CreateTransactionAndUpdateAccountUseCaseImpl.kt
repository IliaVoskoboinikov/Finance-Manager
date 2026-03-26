package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.rollbackOnError
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

class CreateTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : CreateTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем аккаунт. Если ошибка - откатываем транзакцию.
            val account = accountRepository.getById(transaction.accountLocalId).rollbackOnError()

            // 2. Рассчитываем новый баланс
            val updatedAccount = accountRecalculateBalance(account, transaction)

            // 3. Обновляем баланс. Если ошибка - откатываем транзакцию.
            accountRepository.update(updatedAccount).rollbackOnError()

            // 4. Создаём транзакцию. Если ошибка - откатываем транзакцию.
            transactionRepository.create(transaction).rollbackOnError()

            DomainResult.Success(Unit)
        }
    }

    private fun accountRecalculateBalance(account: Account, transaction: Transaction): Account {
        val newBalance = AccountBalanceCalculator.calculate(
            currentBalance = account.balance,
            transactionAmount = transaction.amount,
            type = transaction.type
        )

        return account.copy(balance = newBalance)
    }
}
