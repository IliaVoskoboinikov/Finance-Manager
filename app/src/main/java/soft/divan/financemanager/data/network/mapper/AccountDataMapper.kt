package soft.divan.financemanager.data.network.mapper

import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.network.dto.AccountWithStatsDto
import soft.divan.financemanager.data.network.entity.AccountEntity
import soft.divan.financemanager.domain.model.AccountBrief
import java.math.BigDecimal
import javax.inject.Inject

class AccountDataMapper @Inject constructor() {
    fun toEntity(accountDto: AccountDto): AccountEntity {
        return AccountEntity(
            id = accountDto.id,
            userId = accountDto.userId,
            name = accountDto.name,
            balance = accountDto.balance,
            currency = accountDto.currency,
            createdAt = accountDto.createdAt,
            updatedAt = accountDto.updatedAt
        )
    }
}
fun AccountWithStatsDto.toAccountBriefDomain(): AccountBrief = AccountBrief(
    id = this.id,
    name = this.name,
    balance = this.balance.toBigDecimalOrNull() ?: BigDecimal.ZERO,
    currency = this.currency
)
