package soft.divan.financemanager.domain.model

import java.time.LocalDateTime

enum class ChangeType {
    CREATION, MODIFICATION
}

data class AccountHistory(
    val id: Int,
    val accountId: Int,
    val changeType: ChangeType,
    val previousState: AccountState,
    val newState: AccountState,
    val changeTimestamp: LocalDateTime,
    val createdAt: LocalDateTime
)