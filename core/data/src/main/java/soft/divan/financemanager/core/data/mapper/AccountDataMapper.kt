package soft.divan.financemanager.core.data.mapper

//toddo data mapper
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.finansemanager.core.database.entity.AccountEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun AccountDto.toEntity(): AccountEntity = AccountEntity(
    id = this.id,
    userId = this.userId,
    name = this.name,
    balance = this.balance,
    currency = this.currency,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun AccountEntity.toDomain(): Account = Account(
    id = this.id,
    userId = this.userId,
    name = this.name,
    balance = this.balance.toBigDecimal(),
    currency = this.currency,
    createdAt = LocalDateTime.parse(this.createdAt, formatter),
    updatedAt = LocalDateTime.parse(this.updatedAt, formatter)
)

fun AccountWithStatsDto.toAccountBriefDomain(): AccountBrief = AccountBrief(
    id = this.id,
    name = this.name,
    balance = this.balance.toBigDecimalOrNull() ?: BigDecimal.ZERO,
    currency = this.currency
)

fun CreateAccountRequest.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    name = this.name,
    balance = this.balance.toString(),
    currency = this.currency
)