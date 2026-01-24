package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Period
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetTransactionsByPeriodUseCase {
    operator fun invoke(
        isIncome: Boolean,
        period: Period
    ): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>>

}