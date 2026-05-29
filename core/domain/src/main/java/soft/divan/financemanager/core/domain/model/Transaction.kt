package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: String,
    val accountLocalId: String,
    val targetAccountLocalId: String?, // только для transfer
    val currencyId: String,
    val categoryId: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val transactionDate: Instant,
    val comment: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
