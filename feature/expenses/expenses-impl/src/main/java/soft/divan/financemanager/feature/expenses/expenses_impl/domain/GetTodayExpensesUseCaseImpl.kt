package soft.divan.financemanager.feature.expenses.expenses_impl.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

class GetTodayExpensesUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository,

    ) : GetTodayExpensesUseCase {
    override operator fun invoke(): Flow<Triple<List<Transaction>, CurrencyCode, List<Category>>> =
        flow {

            val account = accountRepository.getAccounts().first().first()

            val todayDate = DateHelper.getTodayApiFormat()

            val transactionsFlow =
                transactionRepository.getTransactionsByAccountAndPeriod(
                    accountId = account.id,
                    startDate = todayDate,
                    endDate = todayDate
                )

            val categoriesFlow = categoryRepository.getCategories()
            val currencyFlow = currencyRepository.getCurrency()

            combine(
                transactionsFlow,
                categoriesFlow,
                currencyFlow
            ) { transactions, categories, currency ->

                val categoriesMap = categories.associateBy { it.id }

                val expenses = transactions
                    .filter { transaction ->
                        val category = categoriesMap[transaction.categoryId]
                        category?.isIncome == false
                    }
                    .sortedByDescending { it.transactionDate }

                Triple(expenses, currency, categories)
            }.collect {
                emit(it)
            }
        }
}
