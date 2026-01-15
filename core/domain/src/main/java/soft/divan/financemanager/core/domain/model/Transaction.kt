package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: String,
    val accountLocalId: String,
    val currencyCode: String,
    val categoryId: Int,
    val amount: BigDecimal,
    val transactionDate: LocalDateTime,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)