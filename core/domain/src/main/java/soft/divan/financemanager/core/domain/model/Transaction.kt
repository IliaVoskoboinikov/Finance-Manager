package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: String,
    val accountLocalId: String,
    val currencyCode: String,
    val categoryId: Int,
    val amount: BigDecimal,
    val transactionDate: Instant,
    val comment: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
// Revue me>>
