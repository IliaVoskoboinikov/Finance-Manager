package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.core.currency.CurrencyCode
import soft.divan.core.currency.repository.CurrencyRepository
import soft.divan.financemanager.date_formater.DateHelper
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetExpensesByPeriodUseCase
import soft.divan.finansemanager.account.domain.repository.AccountRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case для получения списка расходов за указанный период.
 *
 * Этот класс инкапсулирует логику получения транзакций за период времени,
 * фильтрации только расходов (транзакций с категорией, не являющейся доходом),
 * сортировки их по дате и обратного упорядочивания.
 *
 * Для получения данных сначала извлекается список аккаунтов через [soft.divan.finansemanager.account.domain.repository.AccountRepository],
 * затем для первого аккаунта получаются транзакции за заданный период через [TransactionRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 * @property transactionRepository репозиторий для работы с транзакциями
 *
 * @see soft.divan.finansemanager.account.domain.repository.AccountRepository
 * @see TransactionRepository
 * @see Transaction
 */
class GetExpensesByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository

    ) : GetExpensesByPeriodUseCase {
    override operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Pair<List<Transaction>, CurrencyCode>> =
        flow {
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

            val currency = currencyRepository.getCurrency().first()

            emit(Pair(filteredCategories, currency))
        }
}
