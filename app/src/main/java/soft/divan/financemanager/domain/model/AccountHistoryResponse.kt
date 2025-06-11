package soft.divan.financemanager.domain.model

import java.math.BigDecimal

data class AccountHistoryResponse(
    val accountId: Int,
    val accountName: String,
    val currency: String,
    val currentBalance: BigDecimal,
    val history: List<AccountHistory>
)