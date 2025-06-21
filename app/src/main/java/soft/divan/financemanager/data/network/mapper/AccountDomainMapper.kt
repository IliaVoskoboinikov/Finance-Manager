package soft.divan.financemanager.data.network.mapper

import soft.divan.financemanager.data.network.entity.AccountEntity
import soft.divan.financemanager.domain.model.Account
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AccountDomainMapper @Inject constructor() {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    fun toDomain(accountEntity: AccountEntity): Account {
        return Account(
            id = accountEntity.id,
            userId = accountEntity.userId,
            name = accountEntity.name,
            balance = accountEntity.balance.toBigDecimal(),
            currency = accountEntity.currency,
            createdAt = LocalDateTime.parse(accountEntity.createdAt, formatter),
            updatedAt = LocalDateTime.parse(accountEntity.updatedAt, formatter)
        )
    }
}