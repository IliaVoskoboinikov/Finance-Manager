package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction
import java.math.BigDecimal

interface GetSumTransactionsUseCase {
    operator fun invoke(transactions: List<Transaction>): BigDecimal
}
