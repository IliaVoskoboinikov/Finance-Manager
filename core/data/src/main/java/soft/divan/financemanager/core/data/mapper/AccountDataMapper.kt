package soft.divan.financemanager.core.data.mapper

//toddo data mapper
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.finansemanager.core.database.entity.AccountEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun AccountDto.toEntity(): AccountEntity = AccountEntity(
    id = id,
    userId = userId,
    name = name,
    balance = balance,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AccountWithStatsDto.toDomain(): Account = Account(
    id = id,
    userId = id,
    name = name,
    balance = balance.toBigDecimal(),
    currency = currency,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter)
)

private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    userId = userId,
    name = name,
    balance = balance.toBigDecimal(),
    currency = currency,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter)
)


fun AccountBrief.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    name = name,
    balance = balance.toString(),
    currency = currency
)

fun AccountWithStatsDto.toEntity(): AccountEntity = AccountEntity(
    id = id,
    userId = id,
    name = name,
    balance = balance,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt
)