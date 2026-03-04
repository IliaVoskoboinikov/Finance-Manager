package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onFailure
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

@Suppress("ReturnCount")
class UpdateTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : UpdateTransactionUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        // 1. Получаем аккаунт
        val account = accountRepository.getById(transaction.accountLocalId)
            .fold(
                onSuccess = { it },
                onFailure = { return DomainResult.Failure(it) }
            )

        // 2. Получаем старую транзакцию
        val oldTransaction = transactionRepository.getById(transaction.id)
            .fold(
                onSuccess = { it },
                onFailure = { return DomainResult.Failure(it) }
            )

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

        // 4. Обновляем баланс
        accountRepository.update(account.copy(balance = newBalance))
            .onFailure { return DomainResult.Failure(it) }

        // 5. Обновляем баланс
        return transactionRepository.update(transaction)
    }
}
