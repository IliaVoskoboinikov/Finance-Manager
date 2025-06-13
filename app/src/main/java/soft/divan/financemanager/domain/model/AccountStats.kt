package soft.divan.financemanager.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountStats(
    val id: Int,
    val name: String,
    val balance: BigDecimal,
    val currency: String,
    val incomeStats: List<StatItem>,
    val expenseStats: List<StatItem>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
