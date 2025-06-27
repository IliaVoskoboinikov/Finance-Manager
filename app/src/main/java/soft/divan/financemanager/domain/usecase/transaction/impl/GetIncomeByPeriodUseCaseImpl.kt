package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetIncomeByPeriodUseCase
import soft.divan.financemanager.domain.util.DateHelper
import java.time.LocalDate
import javax.inject.Inject

class GetIncomeByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : GetIncomeByPeriodUseCase {

    override operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        flow {
            val accounts = accountRepository.getAccounts()
            val account = accounts.first().first()

            val transactions = transactionRepository.getTransactionsByAccountAndPeriod(
                accountId = account.id,
                startDate = DateHelper.dateToApiFormat(startDate),
                endDate = DateHelper.dateToApiFormat(endDate)
            ).first()

            val filteredCategories = transactions.filter { it.category.isIncome }
                .sortedByDescending { it.transactionDate }

            emit(filteredCategories)
        }
}
