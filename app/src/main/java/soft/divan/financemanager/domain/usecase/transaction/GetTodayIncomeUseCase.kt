package soft.divan.financemanager.domain.usecase.transaction

import kotlinx.coroutines.flow.Flow
import soft.divan.core.currency.CurrencyCode
import soft.divan.financemanager.domain.model.Transaction

interface GetTodayIncomeUseCase {
    operator fun invoke(): Flow<Pair<List<Transaction>, CurrencyCode>>
}
