package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetTodayExpensesUseCase
import soft.divan.financemanager.domain.util.DateHelper
import javax.inject.Inject

class GetTodayExpensesUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
): GetTodayExpensesUseCase {
     override operator fun invoke(): Flow<List<Transaction>> = flow {

        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()
        val todayDate = DateHelper.getTodayApiFormat()
        val transaction = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = todayDate,
            endDate = todayDate
        ).first()

        val filteredCategories = transaction.filter {
            !it.category.isIncome
        }.sortedBy {
            it.transactionDate
        }.reversed()
        emit(filteredCategories)
    }

}