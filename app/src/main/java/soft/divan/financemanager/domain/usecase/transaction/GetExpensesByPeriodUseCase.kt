package soft.divan.financemanager.domain.usecase.transaction

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Transaction
import java.time.LocalDate
import java.util.Date

interface GetExpensesByPeriodUseCase {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
}