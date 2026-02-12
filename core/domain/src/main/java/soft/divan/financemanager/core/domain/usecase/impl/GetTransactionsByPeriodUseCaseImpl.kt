package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Period
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import javax.inject.Inject

/**
 * UseCase для получения транзакций за период.
 *
 * Архитектурные принципы:
 * 1. Single Source of Truth — данные только из локальной БД (Room)
 * 2. Offline-first — синхронизация с сервером внутри репозиториев
 * 3. Fully reactive — use case всегда возвращает "живой" Flow
 */

class GetTransactionsByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository
) : GetTransactionsByPeriodUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override operator fun invoke(
        isIncome: Boolean,
        period: Period
    ): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> {
        /*
         * Базовый combine независимых источников:
         * - аккаунты
         * - категории
         * - валюта
         *
         * Эти источники независимы друг от друга.
         * Любое изменение любого из них → пересчёт downstream логики.
         */
        return baseDataFlow()
            /*
             * flatMapLatest:
             * - при любом изменении аккаунтов / категорий / валюты
             * - отменяем старые подписки
             * - пересобираем транзакции корректно
             */
            .flatMapLatest { baseResult ->
                handleBaseResult(baseResult, period, isIncome)
            }
            .distinctUntilChanged() // Защита от повторных эмитов одинакового состояния.
    }

    private fun baseDataFlow(): Flow<DomainResult<Triple<List<Account>, CurrencySymbol, List<Category>>>> =
        combine(
            accountRepository.getAll(),
            categoryRepository.getAll(),
            currencyRepository.get()
        ) { accountsResult, categoriesResult, currency ->

            when {
                accountsResult is DomainResult.Failure -> DomainResult.Failure(accountsResult.error)

                categoriesResult is DomainResult.Failure -> DomainResult.Failure(
                    categoriesResult.error
                )

                else -> {
                    val accounts = (accountsResult as DomainResult.Success).data
                    val categories = (categoriesResult as DomainResult.Success).data

                    DomainResult.Success(
                        Triple(
                            accounts,
                            currency,
                            categories
                        )
                    )
                }
            }
        }

    private fun handleBaseResult(
        baseResult: DomainResult<Triple<List<Account>, CurrencySymbol, List<Category>>>,
        period: Period,
        isIncome: Boolean
    ): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> =
        when (baseResult) {
            is DomainResult.Failure -> {
                // Ошибка базовых данных → просто пробрасываем
                flowOf(baseResult)
            }

            is DomainResult.Success -> {
                val (accounts, currency, categories) = baseResult.data
                /*
                 * Нет аккаунтов → нет транзакций
                 * Это SUCCESS с пустым списком
                 */
                if (accounts.isEmpty()) {
                    emptyTransactionsResult(currency, categories)
                } else {
                    // Подписка на транзакции по каждому аккаунту
                    transactionsFlow(
                        accounts,
                        period,
                        isIncome,
                        currency,
                        categories
                    )
                }
            }
        }

    private fun emptyTransactionsResult(
        currency: CurrencySymbol,
        categories: List<Category>
    ): Flow<DomainResult.Success<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> = flowOf(
        DomainResult.Success(Triple(emptyList(), currency, categories))
    )

    private fun transactionsFlow(
        accounts: List<Account>,
        period: Period,
        isIncome: Boolean,
        currency: CurrencySymbol,
        categories: List<Category>
    ): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> {
        val categoriesMap = categories.associateBy { it.id }

        return combine(
            accounts.map { account ->
                transactionRepository.getByAccountAndPeriod(
                    accountId = account.id,
                    startDate = period.startDate,
                    endDate = period.endDate
                )
            }
        ) { results ->
            // Любая ошибка → ошибка всего use case
            val failure = results.firstOrNull { it is DomainResult.Failure }

            if (failure != null) {
                failure as DomainResult.Failure
            } else {
                DomainResult.Success(
                    Triple(
                        filterAndSortTransactions(results, categoriesMap, isIncome),
                        currency,
                        categories
                    )
                )
            }
        }
    }

    private fun filterAndSortTransactions(
        results: Array<DomainResult<List<Transaction>>>,
        categoriesMap: Map<Int, Category>,
        isIncome: Boolean
    ): List<Transaction> {
        // Агрегация всех транзакций
        val allTransactions = results
            .filterIsInstance<DomainResult.Success<List<Transaction>>>()
            .flatMap { it.data }
        // Фильтрация по типу + сортировка по дате
        val filteredTransactions = allTransactions
            .filter { transaction ->
                categoriesMap[transaction.categoryId]?.isIncome == isIncome
            }
            .sortedByDescending { it.transactionDate }
        return filteredTransactions
    }
}
