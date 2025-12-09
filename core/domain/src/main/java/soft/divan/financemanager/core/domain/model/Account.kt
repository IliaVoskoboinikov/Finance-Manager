package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Account(
    val id: String,
    val name: String,
    val balance: BigDecimal,
    val currency: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
