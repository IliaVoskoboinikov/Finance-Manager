package soft.divan.financemanager.domain.model

import java.math.BigDecimal

data class AccountState(
    val id: Int,
    val name: String,
    val balance: BigDecimal,
    val currency: String
)
