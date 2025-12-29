package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetTodayTransactionsUseCase {
    operator fun invoke(isIncome: Boolean): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>>
}