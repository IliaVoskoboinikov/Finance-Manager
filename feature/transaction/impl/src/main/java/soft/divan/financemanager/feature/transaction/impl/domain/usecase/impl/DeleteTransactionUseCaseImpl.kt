package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onFailure
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

@Suppress("ReturnCount")
class DeleteTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : DeleteTransactionUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        // 1. Получаем аккаунт
        val account = accountRepository.getById(transaction.accountLocalId)
            .fold(
                onSuccess = { it },
                onFailure = { return DomainResult.Failure(it) }
            )

        // 2. Пересчитываем баланс с откатом
        val newBalance = AccountBalanceCalculator.calculate(
            currentBalance = account.balance,
            transactionAmount = transaction.amount,
            type = transaction.type,
            isReverting = true
        )

        // 3. Обновляем баланс
        accountRepository.update(account.copy(balance = newBalance))
            .onFailure { return DomainResult.Failure(it) }

        // 4. Удаляем транзакцию
        return transactionRepository.delete(transaction.id)
    }
}
