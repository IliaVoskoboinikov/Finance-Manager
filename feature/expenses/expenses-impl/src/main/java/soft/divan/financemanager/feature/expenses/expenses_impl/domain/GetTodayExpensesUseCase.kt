package soft.divan.financemanager.feature.expenses.expenses_impl.domain

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction

interface GetTodayExpensesUseCase {
    operator fun invoke(): Flow<Pair<List<Transaction>, CurrencyCode>>
}