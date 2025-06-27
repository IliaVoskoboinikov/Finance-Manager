package soft.divan.financemanager.domain.usecase.transaction.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.TransactionRepository

import soft.divan.financemanager.domain.usecase.transaction.GetTodayIncomeUseCase
import soft.divan.financemanager.domain.util.DateHelper
import javax.inject.Inject

/**
 * Use case для получения доходов пользователя за текущий день.
 *
 * Данный класс реализует бизнес-логику по извлечению всех транзакций текущей даты,
 * относящихся к доходам, для первого аккаунта пользователя. Он формирует поток (`Flow`)
 * отфильтрованных транзакций, отсортированных по дате в убывающем порядке.
 *
 * Применяется в сценариях отображения статистики за день, например, в аналитике
 * по доходам или в виджете главного экрана.
 *
 * Зависимости:
 * - [AccountRepository] — используется для получения всех аккаунтов
 * - [TransactionRepository] — используется для получения транзакций по конкретному аккаунту и периоду
 *
 * @see GetTodayIncomeUseCase
 * @see Transaction
 * @see Account
 */

class GetTodayIncomeUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) : GetTodayIncomeUseCase {

    override operator fun invoke(): Flow<List<Transaction>> = flow {
        val accounts = accountRepository.getAccounts()
        val account = accounts.first().first()

        val todayDate = DateHelper.getTodayApiFormat()

        val transactions = transactionRepository.getTransactionsByAccountAndPeriod(
            accountId = account.id,
            startDate = todayDate,
            endDate = todayDate
        ).first()

        val filteredCategories = transactions
            .filter { it.category.isIncome }
            .sortedByDescending { it.transactionDate }

        emit(filteredCategories)
    }
}
