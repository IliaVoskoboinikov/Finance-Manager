package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.transaction.rollbackOnError
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.util.AccountBalanceCalculator
import javax.inject.Inject

/**
 * Сохраняет правку операции и пересчитывает балансы затронутых счетов.
 *
 * Старая сумма откатывается на счёте **сохранённой** операции, новая применяется на счёте из
 * правки: если пользователь сменил счёт, сумма переезжает со старого на новый.
 */
class UpdateTransactionAndUpdateAccountUseCaseImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : UpdateTransactionAndUpdateAccountUseCase {

    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRunner.runInTransaction {
            // 1. Получаем старую транзакцию: какую сумму и с какого счёта откатывать
            val oldTransaction = transactionRepository.getById(transaction.id).rollbackOnError()

            // 2. Откатываем её на её же счёте
            val oldAccount = accountRepository.getById(oldTransaction.accountLocalId)
                .rollbackOnError()
            val revertedBalance = AccountBalanceCalculator.calculate(
                currentBalance = oldAccount.balance,
                transactionAmount = oldTransaction.amount,
                type = oldTransaction.type,
                isReverting = true
            )

            // 3. Применяем новую. Баланс обновляем только локально: сервер сам пересчитает оба
            // счёта при PUT /transaction (Replace), PUT /account привёл бы к двойному применению.
            if (oldTransaction.accountLocalId == transaction.accountLocalId) {
                val newBalance = AccountBalanceCalculator.calculate(
                    currentBalance = revertedBalance,
                    transactionAmount = transaction.amount,
                    type = transaction.type
                )
                accountRepository.updateBalanceLocal(oldAccount.id, newBalance).rollbackOnError()
            } else {
                accountRepository.updateBalanceLocal(oldAccount.id, revertedBalance)
                    .rollbackOnError()

                val newAccount = accountRepository.getById(transaction.accountLocalId)
                    .rollbackOnError()
                val newBalance = AccountBalanceCalculator.calculate(
                    currentBalance = newAccount.balance,
                    transactionAmount = transaction.amount,
                    type = transaction.type
                )
                accountRepository.updateBalanceLocal(newAccount.id, newBalance).rollbackOnError()
            }

            // 4. Обновляем саму транзакцию
            transactionRepository.update(transaction).rollbackOnError()

            DomainResult.Success(Unit)
        }
    }
}
