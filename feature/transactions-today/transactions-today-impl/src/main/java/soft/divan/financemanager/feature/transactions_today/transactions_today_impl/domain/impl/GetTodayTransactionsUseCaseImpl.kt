package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCase
import javax.inject.Inject

class GetTodayTransactionsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository,
) : GetTodayTransactionsUseCase {

    override operator fun invoke(isIncome: Boolean): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> =
        flow {
            // 1. Аккаунты
            val accountsResult = accountRepository.getAccounts().first()
            if (accountsResult is DomainResult.Failure) {
                emit(accountsResult)
                return@flow
            }
            val accounts = (accountsResult as DomainResult.Success).data

            // 2. Категории
            val categoriesResult = categoryRepository.getCategories().first()
            if (categoriesResult is DomainResult.Failure) {
                emit(categoriesResult)
                return@flow
            }
            val categories = (categoriesResult as DomainResult.Success).data
            val categoriesMap = categories.associateBy { it.id }

            // 3. Валюта
            val currency = currencyRepository.getCurrency().first()

            val todayDate = DateHelper.getTodayApiFormat()

            // 4. Транзакции по всем аккаунтам (параллельно)
            val allTransactionsResult = coroutineScope {
                accounts.map { account ->
                    async {
                        transactionRepository
                            .getTransactionsByAccountAndPeriod(
                                accountId = account.id,
                                startDate = todayDate,
                                endDate = todayDate
                            )
                            .first()
                    }
                }.awaitAll()
            }

            // 5. Проверка на первую ошибку
            val failure = allTransactionsResult.firstOrNull { it is DomainResult.Failure }
            if (failure != null) {
                emit(failure as DomainResult.Failure)
                return@flow
            }

            val allTransactions = allTransactionsResult
                .filterIsInstance<DomainResult.Success<List<Transaction>>>()
                .flatMap { it.data }

            // 6. Фильтрация по типу
            val filteredTransactions = allTransactions.filter { transaction ->
                categoriesMap[transaction.categoryId]?.isIncome == isIncome
            }

            // 7. Сортировка
            val sortedTransactions = filteredTransactions
                .sortedByDescending { it.transactionDate }

            // 8. Успех
            emit(
                DomainResult.Success(
                    Triple(
                        sortedTransactions,
                        currency,
                        categories
                    )
                )
            )
        }
}