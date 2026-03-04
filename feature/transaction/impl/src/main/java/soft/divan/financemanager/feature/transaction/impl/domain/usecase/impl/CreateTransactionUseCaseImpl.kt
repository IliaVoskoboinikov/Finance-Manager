package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onFailure
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

@Suppress("ReturnCount")
class CreateTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : CreateTransactionUseCase {
    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        // 1. Получаем аккаунт
        val account = accountRepository.getById(transaction.accountLocalId)
            .fold(
                onSuccess = { it },
                onFailure = { return DomainResult.Failure(it) }
            )

        // 2. Считаем новый баланс
        val newBalance = AccountBalanceCalculator.calculate(
            currentBalance = account.balance,
            transactionAmount = transaction.amount,
            type = transaction.type
        )

        // 3. Обновляем баланс
        accountRepository.update(account.copy(balance = newBalance))
            .onFailure { return DomainResult.Failure(it) }

        // 4. Создаём транзакцию
        return transactionRepository.create(transaction)
    }
}
