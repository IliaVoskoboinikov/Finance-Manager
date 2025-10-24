package soft.divan.financemanager.feature.income.income_impl.domain.usecase

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
import javax.inject.Inject

class GetTodayIncomeUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository,

) : GetTodayIncomeUseCase {

    override operator fun invoke(): Flow<Triple<List<Transaction>, CurrencyCode, List<Category>>> =
        flow {

        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()

        val todayDate = DateHelper.getTodayApiFormat()

        val transactions = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = todayDate,
            endDate = todayDate
        ).first()


            val categories = categoryRepository.getCategories().first()
            val categoriesMap = categories.associateBy { it.id }

            val incomes = transactions.filter { transaction ->
                val category = categoriesMap[transaction.categoryId]
                category?.isIncome == true
            }.sortedByDescending { it.transactionDate }

        val currency = currencyRepository.getCurrency().first()

            emit(Triple(incomes, currency, categories))
    }
}