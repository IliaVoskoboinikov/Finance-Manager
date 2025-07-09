package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.core.currency.CurrencyCode
import soft.divan.core.currency.repository.CurrencyRepository
import soft.divan.financemanager.date_formater.DateHelper
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.financemanager.domain.usecase.transaction.GetTodayExpensesUseCase
import soft.divan.finansemanager.account.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Use case для получения расходов пользователя за текущий день.
 *
 * Выполняет выборку всех аккаунтов пользователя, получает первый по списку аккаунт,
 * затем запрашивает список транзакций за сегодняшний день и фильтрует их по категории:
 * возвращаются только транзакции, не являющиеся доходами (`!it.category.isIncome`).
 *
 * Результат сортируется по дате транзакции в убывающем порядке (от новых к старым).
 *
 * Используется для отображения расходов пользователя за текущий день на главном экране,
 * в отчетах или виджетах.
 *
 * Зависимости:
 * - [soft.divan.finansemanager.account.domain.repository.AccountRepository] — для получения списка аккаунтов
 * - [TransactionRepository] — для получения транзакций по дате
 *
 * @see Transaction
 * @see Account
 */
class GetTodayExpensesUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository
) : GetTodayExpensesUseCase {
    override operator fun invoke(): Flow<Pair<List<Transaction>, CurrencyCode>> = flow {

        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()
        val todayDate = DateHelper.getTodayApiFormat()
        val transaction = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = todayDate,
            endDate = todayDate
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
