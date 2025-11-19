package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.util.DateHelper
import java.time.LocalDate
import javax.inject.Inject

class GetTransactionsByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository,
) : GetTransactionsByPeriodUseCase {
    override operator fun invoke(
        isIncome: Boolean,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Triple<List<Transaction>, CurrencyCode, List<Category>>> = flow {

        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()
        val transactions = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = DateHelper.dateToApiFormat(startDate),
            endDate = DateHelper.dateToApiFormat(endDate)
        ).first()

        val categories = categoryRepository.getCategories().first()
        val categoriesMap = categories.associateBy { it.id }

        val expenses = transactions.filter { transaction ->
            val category = categoriesMap[transaction.categoryId]
            category?.isIncome == false
        }.sortedByDescending { it.transactionDate }


        val currency = currencyRepository.getCurrency().first()

        emit(Triple(expenses, currency, categories))
    }
}
