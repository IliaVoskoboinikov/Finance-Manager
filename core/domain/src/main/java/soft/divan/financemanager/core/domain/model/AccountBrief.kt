package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal

data class AccountBrief(
    val id: Int,
    val name: String,
    val balance: BigDecimal,
    val currency: String
)
