package soft.divan.financemanager.domain.model

import soft.divan.financemanager.category.domain.model.Category
import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: Int,
    val accountId: Int,
    val category: Category,
    val amount: BigDecimal,
    val transactionDate: LocalDateTime,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
