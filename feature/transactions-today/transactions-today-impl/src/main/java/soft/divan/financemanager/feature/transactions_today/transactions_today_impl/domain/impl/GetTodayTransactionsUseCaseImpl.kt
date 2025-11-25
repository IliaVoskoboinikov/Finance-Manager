package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCase
import javax.inject.Inject

class GetTodayTransactionsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository,

    ) : GetTodayTransactionsUseCase {
    override operator fun invoke(isIncome: Boolean): Flow<Triple<List<Transaction>, CurrencyCode, List<Category>>> =
        flow {
            // 1) Загружаем все аккаунты
            val allAccounts = accountRepository.getAccounts().first()

            // 2) Загружаем категории и формируем map
            val categories = categoryRepository.getCategories().first()
            val categoriesMap = categories.associateBy { it.id }

            // 3) Загружаем валюту
            val currency = currencyRepository.getCurrency().first()

            // 4) Получаем сегодняшнюю дату
            val todayDate = DateHelper.getTodayApiFormat()

            // 5) Загружаем транзакции по всем аккаунтам ПАРАЛЛЕЛЬНО
            val allTransactions = coroutineScope {
                allAccounts.map { account ->
                    async {
                        transactionRepository
                            .getTransactionsByAccountAndPeriod(
                                accountId = account.id,
                                startDate = todayDate,
                                endDate = todayDate
                            )
                            .first()
                    }
                }.awaitAll().flatten()
            }

            // 6) Фильтруем по типу доход / расход
            val filtered = allTransactions.filter { tx ->
                categoriesMap[tx.categoryId]?.isIncome == isIncome
            }

            // 7) Сортируем по дате
            val sorted = filtered.sortedByDescending { it.transactionDate }

            // 8) Эмитим итог
            emit(Triple(sorted, currency, categories))
        }
}