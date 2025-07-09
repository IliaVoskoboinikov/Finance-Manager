package soft.divan.finansemanager.account.domain.model

import java.math.BigDecimal

data class CreateAccountRequest(
    val name: String,
    val balance: BigDecimal,
    val currency: String
)