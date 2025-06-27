package soft.divan.financemanager.domain.usecase.transaction.impl

import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.usecase.transaction.GetSumTransactionsUseCase
import java.math.BigDecimal

import javax.inject.Inject

class GetSumTransactionsUseCaseImpl @Inject constructor(): GetSumTransactionsUseCase {
     override operator fun invoke(transactions: List<Transaction>): BigDecimal {
        return transactions.sumOf { it.amount }

    }
}