package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.domain.model.CurrencyCode
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.CurrencyRepository
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetIncomeByPeriodUseCase
import soft.divan.financemanager.domain.util.DateHelper
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
 * - получение списка аккаунтов из [AccountRepository]
 * - получение транзакций за период из [TransactionRepository]
 *
 * @property accountRepository репозиторий для доступа к аккаунтам пользователя
 * @property transactionRepository репозиторий для доступа к транзакциям
 *
 * @see AccountRepository
 * @see TransactionRepository
 * @see Transaction
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
