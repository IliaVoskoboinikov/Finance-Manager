package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Account(
    val id: String,
    val name: String,
    val balance: BigDecimal,
    val currencyId: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
