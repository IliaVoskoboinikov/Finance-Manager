package soft.divan.financemanager.domain.usecase.transaction

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.model.Transaction

interface GetTodayExpensesUseCase {
    operator fun invoke(): Flow<List<Transaction>>
}