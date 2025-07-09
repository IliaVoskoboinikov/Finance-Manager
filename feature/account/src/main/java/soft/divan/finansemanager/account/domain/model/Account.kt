package soft.divan.finansemanager.account.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Account(
    val id: Int,
    val userId: Int,
    val name: String,
    val balance: BigDecimal,
    val currency: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
