package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetExpensesByPeriodUseCase
import soft.divan.financemanager.domain.util.DateHelper
import java.time.LocalDate

import java.util.Date
import javax.inject.Inject

class GetExpensesByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,

    ): GetExpensesByPeriodUseCase {
    override operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> = flow {
        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()
        val transaction = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = DateHelper.dateToApiFormat(startDate),
            endDate = DateHelper.dateToApiFormat(endDate)
        ).first()

        val filteredCategories = transaction.filter {
            !it.category.isIncome
        }.sortedBy {
            it.transactionDate
        }.reversed()

        emit(filteredCategories)
    }
}
