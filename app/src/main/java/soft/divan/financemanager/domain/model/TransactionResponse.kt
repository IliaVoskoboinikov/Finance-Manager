package soft.divan.financemanager.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionFull(
    val id: Int,
    val account: AccountBrief,
    val category: Category,
    val amount: BigDecimal,
    val transactionDate: LocalDateTime,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
