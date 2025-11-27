package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction

interface GetTodayTransactionsUseCase {
    operator fun invoke(isIncome: Boolean): Flow<Triple<List<Transaction>, CurrencySymbol, List<Category>>>
}