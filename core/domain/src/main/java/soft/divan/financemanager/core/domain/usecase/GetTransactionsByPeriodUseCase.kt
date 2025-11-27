package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import java.time.LocalDate

interface GetTransactionsByPeriodUseCase {
    operator fun invoke(
        isIncome: Boolean,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Triple<List<Transaction>, CurrencySymbol, List<Category>>>
}