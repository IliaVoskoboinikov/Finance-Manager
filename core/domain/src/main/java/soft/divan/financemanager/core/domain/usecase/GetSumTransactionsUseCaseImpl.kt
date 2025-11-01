package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction
import java.math.BigDecimal
import javax.inject.Inject


class GetSumTransactionsUseCaseImpl @Inject constructor() : GetSumTransactionsUseCase {
    override operator fun invoke(transactions: List<Transaction>): BigDecimal {
        return transactions.sumOf { it.amount }
    }
}