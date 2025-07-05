package soft.divan.financemanager.domain.usecase.transaction

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.CurrencyCode
import soft.divan.financemanager.domain.model.Transaction
import java.time.LocalDate

interface GetIncomeByPeriodUseCase {
    operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Pair<List<Transaction>, CurrencyCode>>
}
