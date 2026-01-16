package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import java.time.LocalDate
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
    private val categoryRepository: CategoryRepository,
) : GetTransactionsByPeriodUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override operator fun invoke(
        isIncome: Boolean,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> {

        val startDateApi = DateHelper.dateToApiFormat(startDate)
        val endDateApi = DateHelper.dateToApiFormat(endDate)

        /**
         * Базовый combine независимых источников:
         * - аккаунты
         * - категории
         * - валюта
         *
         * Эти источники независимы друг от друга.
         * Любое изменение любого из них → пересчёт downstream логики.
         */
        return combine(
            accountRepository.getAccounts(),
            categoryRepository.getCategories(),
            currencyRepository.getCurrency()
        ) { accountsResult, categoriesResult, currency ->

            when {
                accountsResult is DomainResult.Failure -> DomainResult.Failure(accountsResult.error)

                categoriesResult is DomainResult.Failure -> DomainResult.Failure(categoriesResult.error)

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
            /**
             * flatMapLatest:
             * - при любом изменении аккаунтов / категорий / валюты
             * - отменяем старые подписки
             * - пересобираем транзакции корректно
             */
            .flatMapLatest { baseResult ->
                when (baseResult) {

                    is DomainResult.Failure -> {
                        // Ошибка базовых данных → просто пробрасываем
                        flowOf(baseResult)
                    }

                    is DomainResult.Success -> {
                        val (accounts, currency, categories) = baseResult.data
                        val categoriesMap = categories.associateBy { it.id }

                        /**
                         * Нет аккаунтов → нет транзакций
                         * Это SUCCESS с пустым списком
                         */
                        if (accounts.isEmpty()) {
                            flowOf(
                                DomainResult.Success(
                                    Triple(
                                        emptyList(),
                                        currency,
                                        categories
                                    )
                                )
                            )
                        } else {
                            /**
                             * Подписка на транзакции по каждому аккаунту
                             */
                            combine(
                                accounts.map { account ->
                                    transactionRepository.getTransactionsByAccountAndPeriod(
                                        accountId = account.id,
                                        startDate = startDateApi,
                                        endDate = endDateApi
                                    )
                                }
                            ) { transactionResults ->

                                /**
                                 * Любая ошибка → ошибка всего use case
                                 */
                                val failure = transactionResults
                                    .firstOrNull { it is DomainResult.Failure }

                                if (failure != null) {
                                    failure as DomainResult.Failure
                                } else {
                                    /**
                                     * Агрегация всех транзакций
                                     */
                                    val allTransactions = transactionResults
                                        .filterIsInstance<DomainResult.Success<List<Transaction>>>()
                                        .flatMap { it.data }

                                    /**
                                     * Фильтрация по типу
                                     * + сортировка по дате
                                     */
                                    val filteredTransactions = allTransactions
                                        .filter { transaction ->
                                            categoriesMap[transaction.categoryId]?.isIncome == isIncome
                                        }
                                        .sortedByDescending { it.transactionDate }

                                    DomainResult.Success(
                                        Triple(
                                            filteredTransactions,
                                            currency,
                                            categories
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            /**
             * Защита от повторных эмитов одинакового состояния.
             */
            .distinctUntilChanged()
    }
}
