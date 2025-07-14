package soft.divan.financemanager.feature.income.income_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.util.DateHelper
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case для получения списка доходов за заданный период.
 *
 * Инкапсулирует логику получения транзакций по первому аккаунту пользователя
 * за указанный период, фильтрации только доходных транзакций,
 * и сортировки их по дате в порядке убывания (от новых к старым).
 *
 * Для получения данных последовательно вызываются методы:
 * - получение списка аккаунтов из [soft.divan.financemanager.core.domain.repository.AccountRepository]
 * - получение транзакций за период из [soft.divan.financemanager.core.domain.repository.TransactionRepository]
 *
 * @property accountRepository репозиторий для доступа к аккаунтам пользователя
 * @property transactionRepository репозиторий для доступа к транзакциям
 *
 * @see soft.divan.financemanager.core.domain.repository.AccountRepository
 * @see soft.divan.financemanager.core.domain.repository.TransactionRepository
 * @see soft.divan.financemanager.core.domain.model.Transaction
 */
class GetIncomeByPeriodUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository

) : GetIncomeByPeriodUseCase {

    override operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Pair<List<Transaction>, CurrencyCode>> =
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

            val currency = currencyRepository.getCurrency().first()

            emit(Pair(filteredCategories, currency))
        }
}