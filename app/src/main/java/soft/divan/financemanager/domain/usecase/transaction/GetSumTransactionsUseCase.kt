package soft.divan.financemanager.domain.usecase.transaction

import soft.divan.financemanager.domain.model.Transaction
import java.math.BigDecimal

interface GetSumTransactionsUseCase {
    operator fun invoke(transactions: List<Transaction>): BigDecimal
}
