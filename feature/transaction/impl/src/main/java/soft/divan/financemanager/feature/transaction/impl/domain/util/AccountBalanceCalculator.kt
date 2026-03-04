package soft.divan.financemanager.feature.transaction.impl.domain.util

import soft.divan.financemanager.core.domain.model.TransactionType
import java.math.BigDecimal

object AccountBalanceCalculator {
    fun calculate(
        currentBalance: BigDecimal,
        transactionAmount: BigDecimal,
        type: TransactionType,
        isReverting: Boolean = false
    ): BigDecimal {
        val amount = if (isReverting) transactionAmount.negate() else transactionAmount
        return when (type) {
            TransactionType.INCOME, TransactionType.TRANSFER_IN -> currentBalance + amount
            TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> currentBalance - amount
            TransactionType.ADJUSTMENT -> currentBalance + amount
        }
    }
}
