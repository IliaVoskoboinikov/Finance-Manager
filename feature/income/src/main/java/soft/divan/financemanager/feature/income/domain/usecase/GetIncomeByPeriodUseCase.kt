package soft.divan.financemanager.feature.income.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import java.time.LocalDate

interface GetIncomeByPeriodUseCase {
    operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Pair<List<Transaction>, CurrencyCode>>
}