package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import java.time.LocalDateTime
import java.time.ZoneOffset

fun TransactionDto.toEntity(
    localId: String,
    accountLocalId: String,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    accountServerId = account.id,
    categoryId = category.id,
    currencyCode = account.currency,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun TransactionEntity.toDto(accountServerId: Int): TransactionRequestDto = TransactionRequestDto(
    accountId = accountServerId,
    categoryId = categoryId,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = localId,
    accountLocalId = accountLocalId,
    currencyCode = currencyCode,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    transactionDate = LocalDateTime.parse(transactionDate, formatter),
    comment = comment,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter)
)


fun Transaction.toDto(accountIdServer: Int): TransactionRequestDto = TransactionRequestDto(
    accountId = accountIdServer,
    categoryId = categoryId,
    amount = amount.toString(),
    transactionDate = transactionDate.atOffset(ZoneOffset.UTC).format(formatter),
    comment = comment
)

fun Transaction.toEntity(
    serverId: Int?,
    accountServerId: Int?,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = id,
    serverId = serverId,
    accountLocalId = accountLocalId,
    accountServerId = accountServerId,
    categoryId = categoryId,
    currencyCode = currencyCode,
    amount = amount.toPlainString(),
    transactionDate = transactionDate.atOffset(ZoneOffset.UTC).format(formatter),
    comment = comment.orEmpty(),
    createdAt = createdAt.atOffset(ZoneOffset.UTC).format(formatter),
    updatedAt = updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
    syncStatus = syncStatus
)


fun TransactionResponseCreateDto.toEntity(
    localId: String,
    accountLocalId: String,
    currencyCode: String,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    accountServerId = accountId,
    categoryId = categoryId,
    currencyCode = currencyCode,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)