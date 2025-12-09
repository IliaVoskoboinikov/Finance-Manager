package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun AccountDto.toEntity(localId: String, syncStatus: SyncStatus): AccountEntity = AccountEntity(
    localId = localId,
    serverId = id,
    name = name,
    balance = balance,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun Account.toEntity(serverId: Int?, syncStatus: SyncStatus): AccountEntity = AccountEntity(
    localId = id,
    serverId = serverId,
    name = name,
    balance = balance.toString(),
    currency = currency,
    createdAt = createdAt.atOffset(ZoneOffset.UTC).format(formatter),
    updatedAt = updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
    syncStatus = syncStatus
)

val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun AccountEntity.toDomain(): Account = Account(
    id = localId,
    name = name,
    balance = balance.toBigDecimal(),
    currency = currency,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter)
)

fun Account.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    name = name,
    balance = balance.toString(),
    currency = currency
)

fun AccountEntity.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    name = name,
    balance = balance,
    currency = currency
)

fun AccountWithStatsDto.toEntity(localId: String, syncStatus: SyncStatus): AccountEntity = AccountEntity(
    localId = localId,
    serverId = id,
    name = name,
    balance = balance,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)